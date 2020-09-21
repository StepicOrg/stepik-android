package org.stepik.android.data.review_session.repository

import org.stepik.android.domain.review_session.model.ReviewSession
import org.stepik.android.data.review_session.source.ReviewSessionCacheDataSource
import org.stepik.android.data.review_session.source.ReviewSessionRemoteDataSource
import org.stepik.android.domain.review_session.repository.ReviewSessionRepository
import io.reactivex.Single
import org.stepik.android.data.base.repository.delegate.SingleRepositoryDelegate
import org.stepik.android.domain.base.DataSourceType
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class ReviewSessionRepositoryImpl
@Inject
constructor(
    private val reviewSessionCacheDataSource: ReviewSessionCacheDataSource,
    private val reviewSessionRemoteDataSource: ReviewSessionRemoteDataSource
) : ReviewSessionRepository {
    private val delegate =
        SingleRepositoryDelegate(
            reviewSessionRemoteDataSource::getReviewSession,
            reviewSessionCacheDataSource::getReviewSession,
            reviewSessionCacheDataSource::saveReviewSession
        )

    override fun createReviewSession(submissionId: Long): Single<ReviewSession> =
        reviewSessionRemoteDataSource
            .createReviewSession(submissionId)
            .doCompletableOnSuccess(reviewSessionCacheDataSource::saveReviewSession)

    override fun getReviewSession(
        id: Long,
        primarySourceType: DataSourceType
    ): Single<ReviewSession> =
        delegate.get(id, primarySourceType, allowFallback = true)
}