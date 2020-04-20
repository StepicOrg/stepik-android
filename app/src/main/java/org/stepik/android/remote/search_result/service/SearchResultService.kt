package org.stepik.android.remote.search_result.service

import io.reactivex.Single
import org.stepik.android.remote.search_result.model.SearchResultResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface SearchResultService {
    @GET("api/search-results?is_popular=true&is_public=true&type=course")
    fun getSearchResults(@QueryMap query: Map<String, String>): Single<SearchResultResponse>
}