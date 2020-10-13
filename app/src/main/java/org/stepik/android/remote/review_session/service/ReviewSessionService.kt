package org.stepik.android.remote.review_session.service

import org.stepik.android.remote.review_session.model.ReviewSessionResponse
import io.reactivex.Single
import org.stepik.android.remote.review_session.model.ReviewSessionRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReviewSessionService {
    @POST("api/review-sessions")
    fun createReviewSession(@Body body: ReviewSessionRequest): Single<ReviewSessionResponse>

    @GET("api/review-sessions")
    fun getReviewSessions(@Query("ids[]") ids: List<Long>): Single<ReviewSessionResponse>
}