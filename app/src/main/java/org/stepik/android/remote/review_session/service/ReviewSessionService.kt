package org.stepik.android.remote.review_session.service

import org.stepik.android.remote.review_session.model.ReviewSessionResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ReviewSessionService {
    @GET("api/review-sessions")
    fun getReviewSessions(@Query("ids[]") ids: List<Long>): Single<ReviewSessionResponse>
}