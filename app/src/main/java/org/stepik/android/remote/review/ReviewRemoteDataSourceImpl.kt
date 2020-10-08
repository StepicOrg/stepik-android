package org.stepik.android.remote.review

import io.reactivex.Single
import org.stepik.android.data.review.source.ReviewRemoteDataSource
import org.stepik.android.domain.review.model.Review
import org.stepik.android.remote.review.model.ReviewRequest
import org.stepik.android.remote.review.service.ReviewService
import javax.inject.Inject

class ReviewRemoteDataSourceImpl
@Inject
constructor(
    private val reviewService: ReviewService
) : ReviewRemoteDataSource {
    override fun createReview(sessionId: Long): Single<Review> =
        reviewService
            .createReview(ReviewRequest(Review(id = 0, session = sessionId)))
            .map { it.reviews.first() }
}