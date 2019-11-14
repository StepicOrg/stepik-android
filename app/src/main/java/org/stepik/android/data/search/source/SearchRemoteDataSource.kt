package org.stepik.android.data.search.source

import io.reactivex.Single
import org.stepik.android.remote.search.model.QueriesResponse
import org.stepik.android.remote.search.model.SearchResultResponse

interface SearchRemoteDataSource {
    fun getSearchResultsCourses(page: Int, rawQuery: String?): Single<SearchResultResponse>
    fun getSearchQueries(query: String): Single<QueriesResponse>
}