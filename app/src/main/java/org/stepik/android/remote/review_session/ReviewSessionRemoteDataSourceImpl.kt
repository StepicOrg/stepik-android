package org.stepik.android.remote.review_session

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.review_session.source.ReviewSessionRemoteDataSource
import org.stepik.android.domain.review_session.model.ReviewSession
import org.stepik.android.domain.review_session.model.ReviewSessionData
import org.stepik.android.remote.review_session.model.ReviewSessionRequest
import org.stepik.android.remote.review_session.model.ReviewSessionResponse
import org.stepik.android.remote.review_session.service.ReviewSessionService
import javax.inject.Inject

class ReviewSessionRemoteDataSourceImpl
@Inject
constructor(
    private val reviewSessionService: ReviewSessionService
) : ReviewSessionRemoteDataSource {
    private val mapper =
        Function { response: ReviewSessionResponse ->
            val attempts = response.attempts.associateBy { it.id }
            val submissions = response.submissions.associateBy { it.id }

            response.reviewSessions.map { session ->
                val submission = submissions.getValue(session.submission)
                val attempt = attempts.getValue(submission.attempt)

                ReviewSessionData(session, submission, attempt)
            }
        }

    override fun createReviewSession(submissionId: Long): Single<ReviewSessionData> =
        reviewSessionService
            .createReviewSession(ReviewSessionRequest(submissionId))
            .map { mapper.apply(it).first() }

    override fun getReviewSessions(ids: List<Long>): Single<List<ReviewSessionData>> =
        reviewSessionService
            .getReviewSessions(ids)
            .map(mapper)
}