package org.stepik.android.presentation.step_quiz_text

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.step_quiz.interactor.StepQuizInteractor
import org.stepik.android.model.Reply
import org.stepik.android.model.Submission
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class TextStepQuizPresenter
@Inject
constructor(
    private val stepQuizInteractor: StepQuizInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<TextStepQuizView>() {
    private var state: TextStepQuizView.State = TextStepQuizView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: TextStepQuizView) {
        super.attachView(view)
        view.setState(state)
    }

    fun onStepData(stepId: Long) {
        if (state == TextStepQuizView.State.Idle) {
            fetchAttempt(stepId)
        }
    }

    private fun fetchAttempt(stepId: Long) {
        state = TextStepQuizView.State.Loading
        compositeDisposable += stepQuizInteractor
            .getAttempt(stepId)
            .flatMap { attempt ->
                stepQuizInteractor
                    .getSubmission(attempt.id)
                    .map { TextStepQuizView.SubmissionState.Loaded(it) as TextStepQuizView.SubmissionState }
                    .toSingle(TextStepQuizView.SubmissionState.Empty)
                    .map { TextStepQuizView.State.AttemptLoaded(attempt, it) }
            }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { state = it },
                onError = { state = TextStepQuizView.State.NetworkError }
            )
    }

    fun createAttempt(stepId: Long) {
        state = TextStepQuizView.State.Loading
        compositeDisposable += stepQuizInteractor
            .createAttempt(stepId)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { state = TextStepQuizView.State.AttemptLoaded(it, TextStepQuizView.SubmissionState.Empty) },
                onError = { state = TextStepQuizView.State.NetworkError }
            )
    }

    fun createSubmission(reply: Reply) {
        val oldState = (state as? TextStepQuizView.State.AttemptLoaded)
            ?: return

        if (oldState.submissionState is TextStepQuizView.SubmissionState.Loaded) {
            if (oldState.submissionState.submission.status == Submission.Status.WRONG || oldState.submissionState.submission.status == Submission.Status.CORRECT) {
                createAttempt(oldState.attempt.step)
                return
            }
        }

        val submission = Submission(attempt = oldState.attempt.id, reply = reply, status = Submission.Status.EVALUATION)

        state = oldState.copy(submissionState = TextStepQuizView.SubmissionState.Loaded(submission))
        compositeDisposable += stepQuizInteractor
            .createSubmission(oldState.attempt.id, reply)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { state = oldState.copy(submissionState = TextStepQuizView.SubmissionState.Loaded(it)) },
                onError = { state = oldState }
            )
    }
}