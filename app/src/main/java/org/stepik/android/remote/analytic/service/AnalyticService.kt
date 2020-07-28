package org.stepik.android.remote.analytic.service

import io.reactivex.Completable
import org.stepik.android.remote.analytic.model.AnalyticBatchRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AnalyticService {
    // TODO Commented for testing purposes, in order not to spam our backend
//    @POST("api/metrics/batch")
    @POST("https://jsonplaceholder.typicode.com/posts")
    fun batch(@Body request: AnalyticBatchRequest): Completable
}