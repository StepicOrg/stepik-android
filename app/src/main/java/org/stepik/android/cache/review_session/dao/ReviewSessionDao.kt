package org.stepik.android.cache.review_session.dao

import org.stepik.android.domain.review_session.model.ReviewSession
import io.reactivex.Completable
import io.reactivex.Single

interface ReviewSessionDao {
    fun getReviewSession(id: Long): Single<ReviewSession>

    fun saveReviewSession(item: ReviewSession): Completable
}