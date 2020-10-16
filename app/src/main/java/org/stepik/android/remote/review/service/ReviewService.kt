package org.stepik.android.remote.review.service

import io.reactivex.Single
import org.stepik.android.remote.review.model.ReviewRequest
import org.stepik.android.remote.review.model.ReviewResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ReviewService {
    @POST("api/reviews")
    fun createReview(@Body body: ReviewRequest): Single<ReviewResponse>
}