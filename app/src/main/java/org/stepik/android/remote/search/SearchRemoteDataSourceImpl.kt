package org.stepik.android.remote.search

import io.reactivex.Single
import org.stepic.droid.model.SearchQuery
import org.stepik.android.data.search.source.SearchRemoteDataSource
import org.stepik.android.remote.search.model.QueriesResponse
import org.stepik.android.remote.search.service.SearchService
import javax.inject.Inject

class SearchRemoteDataSourceImpl
@Inject
constructor(
    private val searchService: SearchService
) : SearchRemoteDataSource {
    override fun getSearchQueries(query: String): Single<List<SearchQuery>> =
        searchService
            .getSearchQueries(query)
            .map(QueriesResponse::queries)
}