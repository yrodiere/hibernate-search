package org.hibernate.search.jsr352.logging.impl;

import org.hibernate.search.exception.SearchException;

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * Hibernate Search log abstraction for the JSR 352 integration.
 *
 * @author Mincong Huang
 */
@MessageLogger(projectCode = "HSEARCH")
public interface Log extends org.hibernate.search.util.logging.impl.Log {

	@Message(id = JSR_352_MESSAGES_START_ID + 1,
			value = "An 'entityManagerFactoryScope' was defined, but the 'entityManagerFactoryReference' parameter is empty."
					+ " Please also set the 'entityManagerFactoryReference' parameter to select an entity manager factory,"
					+ " or do not set the 'entityManagerFactoryScope' to try to use a default entity manager factory."
	)
	SearchException entityManagerFactoryReferenceIsEmpty();

	@Message(id = JSR_352_MESSAGES_START_ID + 2,
			value = "No entity manager factory available in the CDI context with this bean name: '%1$s'."
					+ " Make sure your entity manager factory is a named bean."
	)
	SearchException noAvailableEntityManagerFactoryInCDI(String reference);

	@Message(id = JSR_352_MESSAGES_START_ID + 3,
			value = "Unknown entity manager factory scope: '%1$s'. Please use a supported scope.")
	SearchException unknownEntityManagerFactoryScope(String scopeName);

	@Message(id = JSR_352_MESSAGES_START_ID + 4,
			value = "Exception while retrieving the EntityManagerFactory using @PersistenceUnit."
					+ " This generally happens either because the persistence wasn't configured properly"
					+ " or because there are multiple persistence units."
	)
	SearchException cannotRetrieveEntityManagerFactoryInJsr352();

	@Message(id = JSR_352_MESSAGES_START_ID + 5,
			value = "Multiple entity manager factories have been registered in the CDI context."
					+ " Please provide the bean name for the selected entity manager factory to the batch indexing job through"
					+ " the 'entityManagerFactoryReference' parameter."
	)
	SearchException ambiguousEntityManagerFactoryInJsr352();

	@Message(id = JSR_352_MESSAGES_START_ID + 6,
			value = "No entity manager factory has been created with this persistence unit name yet: '%1$s'."
					+ " Make sure you use the JPA API to create your entity manager factory (use a 'persistence.xml' file)"
					+ " and that the entity manager factory has already been created and wasn't closed before"
					+ " you launch the job."
	)
	SearchException cannotFindEntityManagerFactoryByPUName(String persistentUnitName);

	@Message(id = JSR_352_MESSAGES_START_ID + 7,
			value = "No entity manager factory has been created with this name yet: '%1$s'."
					+ " Make sure your entity manager factory is named (for instance by setting the '%2$s' option)"
					+ " and that the entity manager factory has already been created and wasn't closed before"
					+ " you launch the job."
	)
	SearchException cannotFindEntityManagerFactoryByName(String entityManagerFactoryName, String option);

	@Message(id = JSR_352_MESSAGES_START_ID + 8,
			value = "No entity manager factory has been created yet."
					+ " Make sure that the entity manager factory has already been created and wasn't closed before"
					+ " you launched the job."
	)
	SearchException noEntityManagerFactoryCreated();

	@Message(id = JSR_352_MESSAGES_START_ID + 9,
			value = "Multiple entity manager factories are currently active."
					+ " Please provide the name of the selected persistence unit to the batch indexing job through"
					+ " the 'entityManagerFactoryReference' parameter (you may also use the 'entityManagerFactoryScope'"
					+ " parameter for more referencing options)."
	)
	SearchException tooManyActiveEntityManagerFactories();
}
