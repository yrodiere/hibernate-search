/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.test.integration.jsr352.massindexing;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.BatchStatus;
import javax.batch.runtime.JobExecution;
import javax.inject.Inject;

import org.hibernate.search.jsr352.massindexing.MassIndexingJob;
import org.hibernate.search.jsr352.test.util.JobTestUtil;
import org.hibernate.search.test.integration.wildfly.massindexing.Concert;
import org.hibernate.search.test.integration.wildfly.massindexing.ConcertManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.Assert.assertFalse;

/**
 * @author Mincong Huang
 */
@RunWith(Arquillian.class)
public class MassIndexingJobTransactionTimeoutIT {

	private static final int JOB_TIMEOUT_MS = 10_000;

	private static final int CONCERT_COUNT = 5_000;

	@Inject
	private ConcertManager concertManager;

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create( WebArchive.class, MassIndexingJobTransactionTimeoutIT.class.getSimpleName() + ".war" )
				.addAsResource( "jsr352/persistence.xml", "META-INF/persistence.xml" )
				.addAsResource( "jsr352/make-deployment-as-batch-app.xml", "META-INF/batch-jobs/make-deployment-as-batch-app.xml" ) // WFLY-7000
				.addAsWebInfResource( "jboss-deployment-structure-jsr352.xml", "/jboss-deployment-structure.xml" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" )
				.addPackage( JobTestUtil.class.getPackage() )
				.addPackage( Concert.class.getPackage() );
	}

	@Before
	public void setUp() throws Exception {
		List<Concert> concerts = new LinkedList<>();
		for ( int i = 0; i < CONCERT_COUNT; i++ ) {
			Date now = new Date( System.currentTimeMillis() );
			concerts.add( new Concert( "An artist", now ) );
		}
		concertManager.saveConcerts( concerts );
		Concert.SLOW_DOWN = true;
	}

	@After
	public void tearDown() throws Exception {
		Concert.SLOW_DOWN = false;
	}

	/**
	 * When the transaction timeout property, {@code javax.transaction.global.timeout},
	 * is assigned with a short value, the mass indexing job won't have enough time to
	 * finish the indexation.
	 */
	@Test
	public void jobFailsWhenTimeoutIsShort() throws Exception {
		JobOperator jobOperator = BatchRuntime.getJobOperator();
		Properties properties = MassIndexingJob.parameters()
				.forEntity( Concert.class )
				.rowsPerPartition( CONCERT_COUNT )
				.transactionTimeout( 2, TimeUnit.SECONDS )
				.build();
		long executionId = jobOperator.start( MassIndexingJob.NAME, properties );

		JobExecution jobExecution = jobOperator.getJobExecution( executionId );
		JobTestUtil.waitForTermination( jobOperator, jobExecution, JOB_TIMEOUT_MS );

		/*
		 * The result cannot be BatchStatus.COMPLETED, because the transaction timeout
		 * value is too short for the job execution to complete its mission. However,
		 * the batch status cannot be determined as BatchStatus.FAILED neither, because
		 * the JobTestUtil might not have waited enough time to let the batch runtime
		 * return a correct result. So instead of asserting a determinist status, we
		 * only assert that the status cannot be BatchStatus.COMPLETED.
		 */
		assertFalse( jobExecution.getBatchStatus() == BatchStatus.COMPLETED );
	}

}
