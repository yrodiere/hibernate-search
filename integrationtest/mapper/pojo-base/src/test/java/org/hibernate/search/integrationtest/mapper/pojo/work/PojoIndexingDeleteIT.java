/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.integrationtest.mapper.pojo.work;

import java.util.concurrent.CompletionStage;

import org.hibernate.search.mapper.javabean.work.SearchIndexer;
import org.hibernate.search.mapper.javabean.work.SearchIndexingPlan;
import org.hibernate.search.util.impl.integrationtest.common.rule.BackendMock;

public class PojoIndexingDeleteIT extends AbstractPojoIndexingOperationIT {

	@Override
	protected void expectOperation(BackendMock.DocumentWorkCallListContext context, String tenantId,
			String id, String routingKey, String value) {
		context.delete( b -> addWorkInfo( b, tenantId, id, routingKey ) );
	}

	@Override
	protected void addTo(SearchIndexingPlan indexingPlan, int id) {
		indexingPlan.delete( createEntity( id ) );
	}

	@Override
	protected void addTo(SearchIndexingPlan indexingPlan, Object providedId, int id) {
		indexingPlan.delete( providedId, null, createEntity( id ) );
	}

	@Override
	protected void addTo(SearchIndexingPlan indexingPlan, Object providedId, String providedRoutingKey, int id) {
		indexingPlan.delete( providedId, providedRoutingKey, createEntity( id ) );
	}

	@Override
	protected CompletionStage<?> execute(SearchIndexer indexer, int id) {
		return indexer.delete( createEntity( id ) );
	}

	@Override
	protected CompletionStage<?> execute(SearchIndexer indexer, Object providedId, int id) {
		return indexer.delete( providedId, createEntity( id ) );
	}

	@Override
	protected CompletionStage<?> execute(SearchIndexer indexer, Object providedId, String providedRoutingKey, int id) {
		return indexer.delete( providedId, providedRoutingKey, createEntity( id ) );
	}
}
