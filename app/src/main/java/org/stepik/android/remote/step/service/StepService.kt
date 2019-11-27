package org.stepik.android.remote.step.service

import io.reactivex.Single
import org.stepik.android.remote.step.model.StepResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface StepService {
    @GET("api/steps")
    fun getSteps(
        @Query("ids[]") steps: LongArray
    ): Single<StepResponse>

    @GET("api/steps")
    fun getStepsByLessonId(
        @Query("lesson") lessonId: Long
    ): Single<StepResponse>
}