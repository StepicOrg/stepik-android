package org.stepik.android.remote.last_step.service

import io.reactivex.Single
import org.stepik.android.remote.last_step.model.LastStepResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface LastStepService {
    @GET("api/last-steps/{lastStepId}")
    fun getLastStepResponse(@Path("lastStepId") lastStepId: String): Single<LastStepResponse>
}