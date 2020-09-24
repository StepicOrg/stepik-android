package org.stepik.android.data.review_session.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.review_session.model.ReviewSession

interface ReviewSessionCacheDataSource {
    fun getReviewSessions(ids: List<Long>): Single<List<ReviewSession>>

    fun saveReviewSessions(items: List<ReviewSession>): Completable
}