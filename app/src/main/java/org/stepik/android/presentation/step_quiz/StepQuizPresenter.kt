package org.stepik.android.presentation.step_quiz

import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.presentation.step_quiz.dispatcher.StepQuizActionDispatcher
import org.stepik.android.presentation.step_quiz.reducer.StepQuizReducer
import javax.inject.Inject

class StepQuizPresenter
@Inject
constructor(
    private val stepQuizReducer: StepQuizReducer,
    private val stepQuizActionDispatcher: StepQuizActionDispatcher,
) : PresenterBase<StepQuizView>() {
    private var state: StepQuizView.State = StepQuizView.State.Idle
        set(value) {
            field = value
            view?.render(value)
        }

    private val viewActionQueue = ArrayDeque<StepQuizView.Action.ViewAction>()

    private val stepQuizMessageListener = ::onNewMessage

    override fun attachView(view: StepQuizView) {
        super.attachView(view)
        view.render(state)
        while (viewActionQueue.isNotEmpty()) {
            view.onAction(viewActionQueue.removeFirst())
        }
    }

    fun onNewMessage(message: StepQuizView.Message) {
        val (newState, actions) = stepQuizReducer.reduce(state, message)

        state = newState
        actions.forEach(::handleAction)
    }

    private fun handleAction(action: StepQuizView.Action) {
        when (action) {
            is StepQuizView.Action.ViewAction ->
                view?.onAction(action) ?: viewActionQueue.addLast(action)

            else ->
                stepQuizActionDispatcher.handleAction(action, stepQuizMessageListener)
        }
    }
}