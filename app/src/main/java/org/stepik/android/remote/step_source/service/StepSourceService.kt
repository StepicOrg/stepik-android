package org.stepik.android.remote.step_source.service

import io.reactivex.Single
import org.stepik.android.remote.step_source.model.StepSourceRequest
import org.stepik.android.remote.step_source.model.StepSourceResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface StepSourceService {
    /*
     * Step sources
     */
    @GET("api/step-sources")
    fun getStepSources(
        @Query("ids[]") ids: LongArray
    ): Single<StepSourceResponse>

    @PUT("api/step-sources/{stepId}")
    fun saveStepSource(
        @Path("stepId") stepId: Long,
        @Body request: StepSourceRequest
    ): Single<StepSourceResponse>
}