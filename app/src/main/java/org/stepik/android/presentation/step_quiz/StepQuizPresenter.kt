package org.stepik.android.presentation.step_quiz

import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_quiz.interactor.StepQuizInteractor
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class StepQuizPresenter
@Inject
constructor(
    private val stepQuizInteractor: StepQuizInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<StepQuizView>() {
    private var state: StepQuizView.State =
        StepQuizView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: StepQuizView) {
        super.attachView(view)
        view.setState(state)
    }

    fun onStepData(stepWrapper: StepPersistentWrapper, lessonData: LessonData, forceUpdate: Boolean = false) {
        if (state == StepQuizView.State.Idle ||
            state == StepQuizView.State.NetworkError && forceUpdate) {
            fetchAttempt(stepWrapper, lessonData)
        }
    }

    private fun fetchAttempt(stepWrapper: StepPersistentWrapper, lessonData: LessonData) {
        state = StepQuizView.State.Loading
        compositeDisposable += stepQuizInteractor
            .getAttempt(stepWrapper.step.id)
            .flatMap { attempt ->
                zip(getSubmissionState(attempt.id), stepQuizInteractor.getStepRestrictions(stepWrapper, lessonData))
                    .map { (submissionState, stepRestrictions) ->
                        StepQuizView.State.AttemptLoaded(attempt, submissionState, stepRestrictions)
                    }
            }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { state = it },
                onError = { state = StepQuizView.State.NetworkError }
            )
    }

    private fun getSubmissionState(attemptId: Long): Single<StepQuizView.SubmissionState> =
        stepQuizInteractor
            .getSubmission(attemptId)
            .map { StepQuizView.SubmissionState.Loaded(it) as StepQuizView.SubmissionState }
            .toSingle(StepQuizView.SubmissionState.Empty)

    fun createAttempt(stepId: Long) {
        val oldState = (state as? StepQuizView.State.AttemptLoaded)
            ?: return
        
        state = StepQuizView.State.Loading
        compositeDisposable += stepQuizInteractor
            .createAttempt(stepId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { state = StepQuizView.State.AttemptLoaded(it, StepQuizView.SubmissionState.Empty, oldState.restrictions) },
                onError = { state = StepQuizView.State.NetworkError }
            )
    }

    fun createSubmission(reply: Reply) {
        val oldState = (state as? StepQuizView.State.AttemptLoaded)
            ?: return

        val submission = Submission(attempt = oldState.attempt.id, reply = reply, status = Submission.Status.EVALUATION)

        state = oldState.copy(submissionState = StepQuizView.SubmissionState.Loaded(submission))
        compositeDisposable += stepQuizInteractor
            .createSubmission(oldState.attempt.id, reply)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { newSubmission ->
                    state = oldState
                        .copy(
                            submissionState = StepQuizView.SubmissionState.Loaded(newSubmission),
                            restrictions = oldState.restrictions.copy(submissionCount = oldState.restrictions.submissionCount + 1)
                        )
                },
                onError = { state = oldState }
            )
    }

    fun syncReplyState(reply: Reply) {
        val oldState = (state as? StepQuizView.State.AttemptLoaded)
            ?: return

        val submission = Submission(attempt = oldState.attempt.id, reply = reply, status = Submission.Status.LOCAL)
        state = oldState.copy(submissionState = StepQuizView.SubmissionState.Loaded(submission))
    }
}