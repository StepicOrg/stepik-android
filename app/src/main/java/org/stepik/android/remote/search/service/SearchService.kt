package org.stepik.android.remote.search.service

import io.reactivex.Single
import org.stepik.android.remote.search.model.QueriesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {
    @GET("api/queries")
    fun getSearchQueries(@Query("query") query: String?): Single<QueriesResponse>
}