package org.stepik.android.remote.submission.service

import io.reactivex.Single
import org.stepik.android.remote.submission.model.SubmissionRequest
import org.stepik.android.remote.submission.model.SubmissionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface SubmissionService {
    @POST("api/submissions")
    fun createNewSubmission(
        @Body submissionRequest: SubmissionRequest
    ): Single<SubmissionResponse>

    @GET("api/submissions?order=desc")
    fun getSubmissions(
        @Query("attempt") attemptId: Long
    ): Single<SubmissionResponse>

    @GET("api/submissions?order=desc")
    fun getSubmissions(
        @QueryMap queryMap: Map<String, String>
    ): Single<SubmissionResponse>
}