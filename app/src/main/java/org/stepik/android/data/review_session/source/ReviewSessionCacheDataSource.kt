package org.stepik.android.data.review_session.source

import org.stepik.android.domain.review_session.model.ReviewSession
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single

interface ReviewSessionCacheDataSource {
    fun getReviewSession(id: Long): Maybe<ReviewSession>

    fun saveReviewSession(item: ReviewSession): Completable
}