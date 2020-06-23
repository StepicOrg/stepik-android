package org.stepik.android.remote.analytic.service

import io.reactivex.Completable
import org.stepik.android.remote.analytic.model.AnalyticBatchRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AnalyticService {
    @POST("api/metrics/batch")
    fun batch(@Body request: AnalyticBatchRequest): Completable
}