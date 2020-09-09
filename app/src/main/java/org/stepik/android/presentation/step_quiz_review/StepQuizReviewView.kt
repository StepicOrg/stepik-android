package org.stepik.android.presentation.step_quiz_review

interface StepQuizReviewView {
    sealed class State {
        object Idle : State()
        object Loading : State()

        class Content() : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}