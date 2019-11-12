package org.stepik.android.remote.search.service

import io.reactivex.Single
import org.stepic.droid.web.QueriesResponse
import org.stepic.droid.web.SearchResultResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {
    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    fun getSearchResults(
        @Query("page") page: Int,
        @Query(value = "query", encoded = true) encodedQuery: String?,
        @Query("language") lang: String
    ): Single<SearchResultResponse>

    @GET("api/queries")
    fun getSearchQueries(@Query("query") query: String?): Single<QueriesResponse>
}