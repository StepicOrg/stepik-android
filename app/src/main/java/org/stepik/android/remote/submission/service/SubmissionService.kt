package org.stepik.android.remote.submission.service

import io.reactivex.Single
import org.stepik.android.remote.submission.model.SubmissionRequest
import org.stepik.android.remote.submission.model.SubmissionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SubmissionService {
    @POST("api/submissions")
    fun createNewSubmissionReactive(
        @Body submissionRequest: SubmissionRequest
    ): Single<SubmissionResponse>

    @GET("api/submissions?order=desc")
    fun getExistingSubmissionsReactive(
        @Query("attempt") attemptId: Long
    ): Single<SubmissionResponse>

    @GET("api/submissions?order=desc")
    fun getExistingSubmissionsForStepReactive(
        @Query("step") stepId: Long,
        @Query("page") page: Int
    ): Single<SubmissionResponse>

    @GET("api/submissions?order=desc")
    fun getExistingSubmissionsForStepReactive(
        @Query("step") stepId: Long,
        @Query("user") user: Long,
        @Query("page") page: Int
    ): Single<SubmissionResponse>
}