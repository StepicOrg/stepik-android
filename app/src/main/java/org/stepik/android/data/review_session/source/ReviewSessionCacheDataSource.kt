package org.stepik.android.data.review_session.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.review_session.model.ReviewSessionData

interface ReviewSessionCacheDataSource {
    fun getReviewSessions(ids: List<Long>): Single<List<ReviewSessionData>>

    fun saveReviewSessions(items: List<ReviewSessionData>): Completable
}