package org.stepik.android.domain.review.repository

import org.stepik.android.domain.review.model.Review
import io.reactivex.Single

interface ReviewRepository {
    fun createReview(sessionId: Long): Single<Review>
}