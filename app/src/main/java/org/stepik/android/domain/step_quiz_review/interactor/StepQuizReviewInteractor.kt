package org.stepik.android.domain.step_quiz_review.interactor

import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.review_instruction.model.ReviewInstruction
import org.stepik.android.domain.review_instruction.repository.ReviewInstructionRepository
import org.stepik.android.domain.review_session.model.ReviewSessionData
import org.stepik.android.domain.review_session.repository.ReviewSessionRepository
import javax.inject.Inject

class StepQuizReviewInteractor
@Inject
constructor(
    private val reviewSessionRepository: ReviewSessionRepository,
    private val reviewInstructionRepository: ReviewInstructionRepository
) {
    fun createSession(submissionId: Long): Single<ReviewSessionData> =
        reviewSessionRepository.createReviewSession(submissionId)

    fun getReviewSession(instructionId: Long, sessionId: Long): Single<Pair<ReviewInstruction, ReviewSessionData>> =
        Singles
            .zip(
                getInstruction(instructionId),
                reviewSessionRepository.getReviewSession(sessionId, sourceType = DataSourceType.REMOTE)
            )

    fun getInstruction(instructionId: Long): Single<ReviewInstruction> =
        reviewInstructionRepository.getReviewInstruction(instructionId, sourceType = DataSourceType.REMOTE)
}