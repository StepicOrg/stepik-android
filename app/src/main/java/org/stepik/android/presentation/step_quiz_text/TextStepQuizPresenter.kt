package org.stepik.android.presentation.step_quiz_text

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.step_quiz.interactor.StepQuizInteractor
import org.stepik.android.model.Reply
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
                    .map { TextStepQuizView.State.SubmissionLoaded(attempt, it) as TextStepQuizView.State }
                    .toSingle(TextStepQuizView.State.AttemptLoaded(attempt))
            }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { state = it },
                onError = { state = TextStepQuizView.State.NetworkError }
            )
    }

    fun createSubmission(reply: Reply) {
//        val attemptId
    }
}