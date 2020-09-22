package org.stepik.android.domain.step_quiz_review.interactor

import io.reactivex.Single
import org.stepik.android.domain.review_session.model.ReviewSession
import org.stepik.android.domain.review_session.repository.ReviewSessionRepository
import javax.inject.Inject

class StepQuizReviewInteractor
@Inject
constructor(
    private val reviewSessionRepository: ReviewSessionRepository
) {
    fun createSession(submissionId: Long): Single<ReviewSession> =
        reviewSessionRepository.createReviewSession(submissionId)
}