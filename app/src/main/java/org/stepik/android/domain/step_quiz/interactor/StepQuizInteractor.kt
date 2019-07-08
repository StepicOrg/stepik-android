package org.stepik.android.domain.step_quiz.interactor

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.maybeFirst
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_quiz.model.StepQuizRestrictions
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.model.DiscountingPolicyType
import org.stepik.android.model.Reply
import org.stepik.android.model.Step
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.view.injection.step_quiz.StepQuizBus
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StepQuizInteractor
@Inject
constructor(
    @StepQuizBus
    private val stepQuizPublisher: PublishSubject<Long>,

    private val attemptRepository: AttemptRepository,
    private val submissionRepository: SubmissionRepository
) {
    fun getAttempt(stepId: Long): Single<Attempt> =
        attemptRepository
            .getAttemptsForStep(stepId)
            .maybeFirst()
            .filter { it.status == "active" }
            .switchIfEmpty(attemptRepository.createAttemptForStep(stepId))

    fun createAttempt(stepId: Long): Single<Attempt> =
        attemptRepository
            .createAttemptForStep(stepId)

    fun getSubmission(attemptId: Long): Maybe<Submission> =
        submissionRepository
            .getSubmissionsForAttempt(attemptId)
            .maybeFirst()

    fun createSubmission(stepId: Long, attemptId: Long, reply: Reply): Single<Submission> =
        submissionRepository
            .createSubmission(Submission(attempt = attemptId, reply = reply))
            .flatMapObservable {
                Observable
                    .interval(1, TimeUnit.SECONDS)
                    .flatMapMaybe { submissionRepository.getSubmissionsForAttempt(attemptId).maybeFirst() }
                    .skipWhile { it.status == Submission.Status.EVALUATION }
            }
            .firstOrError()
            .doOnSuccess { newSubmission ->
                if (newSubmission.status == Submission.Status.CORRECT) {
                    stepQuizPublisher.onNext(stepId)
                }
            }

    fun getStepRestrictions(stepPersistentWrapper: StepPersistentWrapper, lessonData: LessonData): Single<StepQuizRestrictions> =
        getStepSubmissionCount(stepPersistentWrapper.step.id)
            .map { submissionCount ->
                StepQuizRestrictions(
                    submissionCount = submissionCount,
                    maxSubmissionCount = stepPersistentWrapper
                        .step
                        .maxSubmissionCount
                        .takeIf { stepPersistentWrapper.step.hasSubmissionRestriction }
                        ?: -1,
                    discountingPolicyType = lessonData
                        .section
                        ?.discountingPolicy
                        ?: DiscountingPolicyType.NoDiscount
                )
            }

    private fun getStepSubmissionCount(stepId: Long): Single<Int> =
        submissionRepository
            .getSubmissionsForStep(stepId)
            .map { it.size }

    fun isNeedRecreateAttemptForNewSubmission(step: Step): Boolean =
        when (step.block?.name) {
            AppConstants.TYPE_STRING,
            AppConstants.TYPE_NUMBER,
            AppConstants.TYPE_MATH,
            AppConstants.TYPE_FREE_ANSWER,
                
            AppConstants.TYPE_CODE ->
                false

            else ->
                true
        }
}