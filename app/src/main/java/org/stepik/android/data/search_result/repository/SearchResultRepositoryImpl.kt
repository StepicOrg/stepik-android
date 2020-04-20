package org.stepik.android.data.search_result.repository

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.data.search_result.source.SearchResultRemoteDataSource
import org.stepik.android.domain.search_result.model.SearchResultQuery
import org.stepik.android.domain.search_result.repository.SearchResultRepository
import org.stepik.android.model.SearchResult
import javax.inject.Inject

class SearchResultRepositoryImpl
@Inject
constructor(
    private val searchResultRemoteDataSource: SearchResultRemoteDataSource
) : SearchResultRepository {
    override fun getSearchResults(searchResultQuery: SearchResultQuery): Single<PagedList<SearchResult>> =
        searchResultRemoteDataSource.getSearchResults(searchResultQuery)
}