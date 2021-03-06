/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.backend.lucene.search.query.dsl;

import org.hibernate.search.backend.lucene.search.predicate.dsl.LuceneSearchPredicateFactory;
import org.hibernate.search.engine.search.query.dsl.SearchQueryWhereStep;

public interface LuceneSearchQueryWhereStep<H, LOS>
		extends SearchQueryWhereStep<LuceneSearchQueryOptionsStep<H, LOS>, H, LOS, LuceneSearchPredicateFactory> {

}
