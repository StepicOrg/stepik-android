package org.stepik.android.data.review_session.repository

import io.reactivex.Single
import org.stepik.android.data.base.repository.delegate.ListRepositoryDelegate
import org.stepik.android.data.review_session.source.ReviewSessionCacheDataSource
import org.stepik.android.data.review_session.source.ReviewSessionRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.review_session.model.ReviewSessionData
import org.stepik.android.domain.review_session.repository.ReviewSessionRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class ReviewSessionRepositoryImpl
@Inject
constructor(
    private val reviewSessionCacheDataSource: ReviewSessionCacheDataSource,
    private val reviewSessionRemoteDataSource: ReviewSessionRemoteDataSource
) : ReviewSessionRepository {
    private val listDelegate =
        ListRepositoryDelegate(
            reviewSessionRemoteDataSource::getReviewSessions,
            reviewSessionCacheDataSource::getReviewSessions,
            reviewSessionCacheDataSource::saveReviewSessions
        )

    override fun createReviewSession(submissionId: Long): Single<ReviewSessionData> =
        reviewSessionRemoteDataSource
            .createReviewSession(submissionId)
            .doCompletableOnSuccess { reviewSessionCacheDataSource.saveReviewSessions(listOf(it)) }

    override fun getReviewSessions(
        ids: List<Long>,
        sourceType: DataSourceType
    ): Single<List<ReviewSessionData>> =
        listDelegate.get(ids, sourceType, allowFallback = true)
}