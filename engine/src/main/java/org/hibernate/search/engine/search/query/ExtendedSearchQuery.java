/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.engine.search.query;

/**
 * A base interface for subtypes of {@link SearchQuery} allowing to
 * easily override the result type for all relevant methods.
 *
 * @param <H> The type of query hits.
 * @param <R> The result type (extending {@link SearchResult}).
 * @param <SC> The scroll type (extending {@link SearchScroll}).
 */
public interface ExtendedSearchQuery<H, R extends SearchResult<H>, SC extends SearchScroll<H>>
		extends SearchQuery<H>, ExtendedSearchFetchable<H, R, SC> {

}
