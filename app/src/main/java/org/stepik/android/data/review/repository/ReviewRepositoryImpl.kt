package org.stepik.android.data.review.repository

import org.stepik.android.domain.review.model.Review
import org.stepik.android.data.review.source.ReviewRemoteDataSource
import org.stepik.android.domain.review.repository.ReviewRepository
import io.reactivex.Single
import javax.inject.Inject

class ReviewRepositoryImpl
@Inject
constructor(
    private val reviewRemoteDataSource: ReviewRemoteDataSource
) : ReviewRepository {
    override fun createReview(sessionId: Long): Single<Review> =
        reviewRemoteDataSource.createReview(sessionId)
}