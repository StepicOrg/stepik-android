package org.stepik.android.presentation.step_quiz_review

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.persistence.model.StepPersistentWrapper
import org.stepik.android.domain.step_quiz_review.interactor.StepQuizReviewInteractor
import org.stepik.android.presentation.step_quiz_review.reducer.StepQuizReviewReducer
import ru.nobird.android.presentation.base.PresenterBase
import timber.log.Timber
import javax.inject.Inject

class StepQuizReviewPresenter
@Inject
constructor(
    stepWrapperRxRelay: BehaviorRelay<StepPersistentWrapper>,

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

    init {
        compositeDisposable += stepWrapperRxRelay
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(onNext = { onNewMessage(StepQuizReviewView.Message.InitWithStep(it.step)) })
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
        Timber.d("message = ${message.javaClass.canonicalName}")
        Timber.d("newState = ${newState.javaClass.canonicalName}")
        Timber.d("actions = $actions")

        state = newState
        actions.forEach(::handleAction)
    }

    private fun handleAction(action: StepQuizReviewView.Action) {
        when (action) {
            is StepQuizReviewView.Action.ViewAction -> {
                view?.onAction(action) ?: viewActionQueue.addLast(action)
            }

            is StepQuizReviewView.Action.FetchStepQuizState -> {
                compositeDisposable
            }

            is StepQuizReviewView.Action.FetchReviewSession -> {
                compositeDisposable
            }

            is StepQuizReviewView.Action.CreateSessionWithSubmission -> {
                compositeDisposable += stepQuizReviewInteractor
                    .createSession(action.submissionId)
                    .observeOn(mainScheduler)
                    .subscribeOn(backgroundScheduler)
                    .subscribeBy(
                        onSuccess = { onNewMessage(StepQuizReviewView.Message.SessionCreated(it.session)) },
                        onError = { onNewMessage(StepQuizReviewView.Message.CreateSessionError) }
                    )
            }
        }.hashCode()
    }
}