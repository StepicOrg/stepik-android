package org.stepik.android.remote.review_session

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.review_session.source.ReviewSessionRemoteDataSource
import org.stepik.android.domain.review_session.model.ReviewSession
import org.stepik.android.remote.review_session.model.ReviewSessionRequest
import org.stepik.android.remote.review_session.model.ReviewSessionResponse
import org.stepik.android.remote.review_session.service.ReviewSessionService
import javax.inject.Inject

class ReviewSessionRemoteDataSourceImpl
@Inject
constructor(
    private val reviewSessionService: ReviewSessionService
) : ReviewSessionRemoteDataSource {
    private val mapper = Function(ReviewSessionResponse::reviewSessions)

    override fun createReviewSession(submissionId: Long): Single<ReviewSession> =
        reviewSessionService
            .createReviewSession(ReviewSessionRequest(submissionId))
            .map { it.reviewSessions.first() }

    override fun getReviewSessions(ids: List<Long>): Single<List<ReviewSession>> =
        reviewSessionService
            .getReviewSessions(ids)
            .map(mapper)
}