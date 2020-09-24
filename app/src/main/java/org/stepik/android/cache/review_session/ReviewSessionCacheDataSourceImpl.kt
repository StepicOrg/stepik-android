package org.stepik.android.cache.review_session

import org.stepik.android.cache.review_session.dao.ReviewSessionDao
import org.stepik.android.data.review_session.source.ReviewSessionCacheDataSource
import org.stepik.android.domain.review_session.model.ReviewSession
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import javax.inject.Inject

class ReviewSessionCacheDataSourceImpl
@Inject
constructor(
    private val reviewSessionDao: ReviewSessionDao
) : ReviewSessionCacheDataSource {
    override fun getReviewSessions(ids: List<Long>): Single<List<ReviewSession>> =
        reviewSessionDao
            .getReviewSessions(ids)

    override fun saveReviewSessions(items: List<ReviewSession>): Completable =
        reviewSessionDao.saveReviewSessions(items)
}