/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.jsr352.massindexing.impl.util;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.hcore.util.impl.ContextHelper;
import org.hibernate.search.jsr352.context.jpa.EntityManagerFactoryRegistry;
import org.hibernate.search.jsr352.logging.impl.Log;
import org.hibernate.search.util.logging.impl.LoggerFactory;

/**
 * Utility class for job parameter validation.
 *
 * @author Mincong Huang
 */
public final class ValidationUtil {

	private static final Log log = LoggerFactory.make( Log.class );

	private ValidationUtil() {
		// Private constructor, do not use it.
	}

	public static void validateCheckpointInterval(Integer checkpointInterval, Integer rowsPerPartitions) {
		if ( checkpointInterval != null && rowsPerPartitions != null && checkpointInterval >= rowsPerPartitions ) {
			throw log.illegalCheckpointInterval( checkpointInterval, rowsPerPartitions );
		}
	}

	public static void validatePositive(String parameterName, int parameterValue) {
		if ( parameterValue <= 0 ) {
			throw log.negativeValueOrZero( parameterName, parameterValue );
		}
	}

	public static void validateEntityTypes(
			EntityManagerFactoryRegistry emfRegistry,
			String entityManagerFactoryScope,
			String entityManagerFactoryReference,
			String serializedEntityTypes) {
		EntityManagerFactory emf = JobContextUtil.getEntityManagerFactory(
				emfRegistry,
				entityManagerFactoryScope,
				entityManagerFactoryReference
		);

		ExtendedSearchIntegrator searchIntegrator = ContextHelper.getSearchIntegratorBySF( emf.unwrap( SessionFactory.class ) );
		Set<String> failingTypes = new HashSet<>();
		Set<String> indexedTypes = searchIntegrator
				.getIndexedTypes()
				.stream()
				.map( Class::getName )
				.collect( Collectors.toSet() );

		for ( String type : serializedEntityTypes.split( "," ) ) {
			if ( !indexedTypes.contains( type ) ) {
				failingTypes.add( type );
			}
		}
		if ( failingTypes.size() > 0 ) {
			throw log.failingEntityTypes( String.join( ", ", failingTypes ) );
		}
	}

}
