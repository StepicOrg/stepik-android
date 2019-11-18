package org.stepik.android.remote.attempt.service

import io.reactivex.Single
import org.stepik.android.remote.attempt.model.AttemptRequest
import org.stepik.android.remote.attempt.model.AttemptResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AttemptService {
    @POST("api/attempts")
    fun createNewAttempt(@Body attemptRequest: AttemptRequest): Single<AttemptResponse>

    @GET("api/attempts")
    fun getAttemptsForStep(@Query("step") stepId: Long, @Query("user") userId: Long): Single<AttemptResponse>

    @GET("api/attempts")
    fun getAttemptsForStep(@Query("ids[]") ids: LongArray): Single<AttemptResponse>
}