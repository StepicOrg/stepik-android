package org.stepik.android.remote.search.service

import io.reactivex.Single
import org.stepik.android.remote.search.model.QueriesResponse
import org.stepik.android.remote.search.model.SearchResultResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface SearchService {
    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    fun getSearchResults(@QueryMap query: Map<String, String>): Single<SearchResultResponse>

    @GET("api/queries")
    fun getSearchQueries(@Query("query") query: String?): Single<QueriesResponse>
}