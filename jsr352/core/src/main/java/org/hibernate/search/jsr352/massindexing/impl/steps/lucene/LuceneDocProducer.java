/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.jsr352.massindexing.impl.steps.lucene;

import java.io.Serializable;

import javax.batch.api.BatchProperty;
import javax.batch.api.chunk.ItemProcessor;
import javax.batch.runtime.context.JobContext;
import javax.batch.runtime.context.StepContext;
import javax.inject.Inject;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.search.backend.AddLuceneWork;
import org.hibernate.search.bridge.TwoWayFieldBridge;
import org.hibernate.search.bridge.spi.ConversionContext;
import org.hibernate.search.bridge.util.impl.ContextualExceptionBridgeHelper;
import org.hibernate.search.engine.impl.HibernateSessionLoadingInitializer;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.engine.spi.DocumentBuilderIndexedEntity;
import org.hibernate.search.engine.spi.EntityIndexBinding;
import org.hibernate.search.hcore.util.impl.ContextHelper;
import org.hibernate.search.jsr352.logging.impl.Log;
import org.hibernate.search.jsr352.massindexing.impl.JobContextData;
import org.hibernate.search.jsr352.massindexing.impl.util.MassIndexingPartitionProperties;
import org.hibernate.search.spi.InstanceInitializer;
import org.hibernate.search.util.logging.impl.LoggerFactory;

/**
 * ItemProcessor receives entities coming from item reader and process then into an AddLuceneWorks. Only one entity is
 * received and processed at each time.
 *
 * @author Mincong Huang
 */
public class LuceneDocProducer implements ItemProcessor {

	private static final Log log = LoggerFactory.make( Log.class );

	@Inject
	private JobContext jobContext;

	@Inject
	private StepContext stepContext;

	@Inject
	@BatchProperty(name = MassIndexingPartitionProperties.ENTITY_NAME)
	private String entityName;

	private EntityManagerFactory emf;

	private Session session;
	private ExtendedSearchIntegrator searchIntegrator;
	private EntityIndexBinding entityIndexBinding;
	private DocumentBuilderIndexedEntity docBuilder;
	private boolean isSetup = false;
	private Class<?> entityType;

	@Override
	public Object processItem(Object item) throws Exception {
		log.debug( "Processing item ..." );
		if ( !isSetup ) {
			setup();
			isSetup = true;
		}
		AddLuceneWork addWork = buildAddLuceneWork( item, entityType );
		return addWork;
	}

	/**
	 * Set up environment for lucene work production.
	 *
	 * @throws ClassNotFoundException if the entityName does not match any indexed class type in the job context data.
	 * @throws NamingException if JNDI lookup for entity manager failed
	 */
	private void setup() throws ClassNotFoundException, NamingException {

		entityType = ( (JobContextData) jobContext.getTransientUserData() )
				.getIndexedType( entityName );
		PartitionContextData partitionData = (PartitionContextData) stepContext.getTransientUserData();
		session = partitionData.getSession();
		searchIntegrator = ContextHelper.getSearchIntegrator( session );
		entityIndexBinding = searchIntegrator.getIndexBindings().get( entityType );
		docBuilder = entityIndexBinding.getDocumentBuilder();

		JobContextData jobData = (JobContextData) jobContext.getTransientUserData();
		emf = jobData.getEntityManagerFactory();
	}

	/**
	 * Build addLuceneWork using input entity. This method is inspired by the current mass indexer implementation.
	 *
	 * @param entity selected entity, obtained from JPA entity manager. It is used to build Lucene work.
	 * @param entityType the class type of selected entity
	 * @return an addLuceneWork
	 */
	private AddLuceneWork buildAddLuceneWork(Object entity, Class<?> entityType) {
		// TODO: tenant ID should not be null
		// Or may it be fine to be null? Gunnar's integration test in Hibernate
		// Search: MassIndexingTimeoutIT does not mention the tenant ID neither
		// (The tenant ID is not included mass indexer setup in the
		// ConcertManager)
		String tenantId = null;
		ConversionContext conversionContext = new ContextualExceptionBridgeHelper();
		final InstanceInitializer sessionInitializer = new HibernateSessionLoadingInitializer(
				(SessionImplementor) session );

		Serializable id = (Serializable) emf.getPersistenceUnitUtil()
				.getIdentifier( entity );
		TwoWayFieldBridge idBridge = docBuilder.getIdBridge();
		conversionContext.pushIdentifierProperty();
		String idInString = null;
		try {
			idInString = conversionContext
					.setClass( entityType )
					.twoWayConversionContext( idBridge )
					.objectToString( id );
			log.debugf( "idInString=%s", idInString );
		}
		finally {
			conversionContext.popProperty();
		}
		AddLuceneWork addWork = docBuilder.createAddWork(
				tenantId,
				entityType,
				entity,
				id,
				idInString,
				sessionInitializer,
				conversionContext );
		return addWork;
	}
}
