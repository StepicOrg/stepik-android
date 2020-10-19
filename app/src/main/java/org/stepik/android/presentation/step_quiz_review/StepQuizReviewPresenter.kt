package org.stepik.android.presentation.step_quiz_review

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.lesson.model.LessonData
import org.stepik.android.domain.step_quiz.interactor.StepQuizInteractor
import org.stepik.android.domain.step_quiz_review.interactor.StepQuizReviewInteractor
import org.stepik.android.presentation.step_quiz.StepQuizView
import org.stepik.android.presentation.step_quiz.dispatcher.StepQuizActionDispatcher
import org.stepik.android.presentation.step_quiz_review.reducer.StepQuizReviewReducer
import ru.nobird.android.presentation.base.PresenterBase
import javax.inject.Inject

class StepQuizReviewPresenter
@Inject
constructor(
    stepWrapperRxRelay: BehaviorRelay<StepPersistentWrapper>,
    lessonData: LessonData,

    private val stepQuizActionDispatcher: StepQuizActionDispatcher,
    private val stepQuizInteractor: StepQuizInteractor,
    private val stepQuizReviewInteractor: StepQuizReviewInteractor,
    private val stepQuizReviewReducer: StepQuizReviewReducer,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<StepQuizReviewView>() {
    private var state: StepQuizReviewView.State = StepQuizReviewView.State.Idle
        set(value) {
            field = value
            view?.render(value)
        }

    private val viewActionQueue = ArrayDeque<StepQuizReviewView.Action.ViewAction>()

    private val stepQuizMessageListener = { message: StepQuizView.Message ->
        onNewMessage(StepQuizReviewView.Message.StepQuizMessage(message))
    }

    init {
        compositeDisposable += stepWrapperRxRelay
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(onNext = { onNewMessage(StepQuizReviewView.Message.InitWithStep(it, lessonData)) })

        compositeDisposable += stepQuizActionDispatcher.disposable
    }

    override fun attachView(view: StepQuizReviewView) {
        super.attachView(view)
        view.render(state)
        while (viewActionQueue.isNotEmpty()) {
            view.onAction(viewActionQueue.removeFirst())
        }
    }

    fun onNewMessage(message: StepQuizReviewView.Message) {
        val (newState, actions) = stepQuizReviewReducer.reduce(state, message)

        state = newState
        actions.forEach(::handleAction)
    }

    private fun handleAction(action: StepQuizReviewView.Action) {
        when (action) {
            is StepQuizReviewView.Action.ViewAction -> {
                view?.onAction(action) ?: viewActionQueue.addLast(action)
            }

            is StepQuizReviewView.Action.FetchStepQuizState -> {
                compositeDisposable += Singles
                    .zip(
                        getAttemptState(action.stepWrapper, action.lessonData),
                        stepQuizReviewInteractor.getStepProgress(action.stepWrapper.step.id, action.lessonData.unit?.id)
                    )
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy(
                        onSuccess = { (quizState, progress) ->
                            onNewMessage(StepQuizReviewView.Message.FetchStepQuizStateSuccess(quizState, progress.firstOrNull()))
                        },
                        onError = { onNewMessage(StepQuizReviewView.Message.InitialFetchError) }
                    )
            }

            is StepQuizReviewView.Action.FetchReviewSession -> {
                compositeDisposable += stepQuizReviewInteractor
                    .getReviewSession(action.stepId, action.unitId, action.instructionId, action.sessionId)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribeBy(
                        onSuccess = { (instruction, sessionData, progress) ->
                            onNewMessage(StepQuizReviewView.Message.FetchReviewSessionSuccess(sessionData, instruction, progress.firstOrNull()))
                        },
                        onError = { onNewMessage(StepQuizReviewView.Message.InitialFetchError) }
                    )
            }

            is StepQuizReviewView.Action.StepQuizAction ->
                stepQuizActionDispatcher.handleAction(action.action, stepQuizMessageListener)

            is StepQuizReviewView.Action.CreateSessionWithSubmission -> {
                compositeDisposable += stepQuizReviewInteractor
                    .createSession(action.submissionId)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribeBy(
                        onSuccess = { (session, instruction) ->
                            onNewMessage(StepQuizReviewView.Message.SessionCreated(session, instruction))
                        },
                        onError = { onNewMessage(StepQuizReviewView.Message.CreateSessionError) }
                    )
            }

            is StepQuizReviewView.Action.CreateReviewWithSession -> {
                compositeDisposable += stepQuizReviewInteractor
                    .createReview(action.sessionId)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribeBy(
                        onSuccess = { review ->
                            onNewMessage(StepQuizReviewView.Message.ReviewCreated(review.id))
                        },
                        onError = { onNewMessage(StepQuizReviewView.Message.StartReviewError) }
                    )
            }
        }
    }

    private fun getAttemptState(stepWrapper: StepPersistentWrapper, lessonData: LessonData): Single<StepQuizView.State.AttemptLoaded> =
        stepQuizInteractor
            .getAttempt(stepWrapper.id)
            .flatMap { attempt ->
                Singles
                    .zip(
                        stepQuizActionDispatcher.getSubmissionState(attempt.id),
                        stepQuizInteractor.getStepRestrictions(stepWrapper, lessonData)
                    )
                    .map { (submissionState, stepRestrictions) ->
                        StepQuizView.State.AttemptLoaded(attempt, submissionState, stepRestrictions)
                    }
            }
}