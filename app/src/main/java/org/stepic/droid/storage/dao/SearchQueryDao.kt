package org.stepic.droid.storage.dao

import org.stepic.droid.model.SearchQuery

interface SearchQueryDao : IDao<SearchQuery> {
    fun getSearchQueries(constraint: String, count: Int): List<SearchQuery>
}