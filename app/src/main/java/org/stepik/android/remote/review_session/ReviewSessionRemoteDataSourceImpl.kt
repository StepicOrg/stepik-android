package org.stepik.android.remote.review_session

import io.reactivex.Single
import org.stepik.android.data.review_session.source.ReviewSessionRemoteDataSource
import org.stepik.android.domain.review_session.model.ReviewSession
import org.stepik.android.remote.review_session.model.ReviewSessionRequest
import org.stepik.android.remote.review_session.service.ReviewSessionService
import javax.inject.Inject

class ReviewSessionRemoteDataSourceImpl
@Inject
constructor(
    private val reviewSessionService: ReviewSessionService,
) : ReviewSessionRemoteDataSource {
    override fun createReviewSession(submissionId: Long): Single<ReviewSession> =
        reviewSessionService
            .createReviewSession(ReviewSessionRequest(submissionId))
            .map { it.reviewSessions.first() }

    override fun getReviewSession(id: Long): Single<ReviewSession> =
        reviewSessionService
            .getReviewSessions(listOf(id))
            .map { it.reviewSessions.first() }
}