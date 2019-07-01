package org.stepik.android.domain.step_quiz.interactor

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.stepic.droid.util.maybeFirst
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.model.attempts.Attempt
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StepQuizInteractor
@Inject
constructor(
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

    fun createSubmission(attemptId: Long, reply: Reply): Single<Submission> =
        submissionRepository
            .createSubmission(Submission(attempt = attemptId, reply = reply))
            .flatMapObservable {
                Observable
                    .interval(1, TimeUnit.SECONDS)
                    .flatMapMaybe { submissionRepository.getSubmissionsForAttempt(attemptId).maybeFirst() }
                    .skipWhile { it.status == Submission.Status.EVALUATION }
            }
            .firstOrError()
}