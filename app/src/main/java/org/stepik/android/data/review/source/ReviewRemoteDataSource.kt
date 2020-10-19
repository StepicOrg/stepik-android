package org.stepik.android.data.review.source

import org.stepik.android.domain.review.model.Review
import io.reactivex.Single

interface ReviewRemoteDataSource {
    fun createReview(sessionId: Long): Single<Review>
}