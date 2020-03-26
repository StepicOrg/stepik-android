package org.stepik.android.domain.search_result.repository

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.model.SearchResult

interface SearchResultRepository {
    fun getSearchResults(searchResultQuery: SearchResultQuery): Single<PagedList<SearchResult>>
}