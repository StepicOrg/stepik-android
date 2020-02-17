package org.stepik.android.presentation.step_quiz_code

import org.stepik.android.model.code.UserCodeRun

interface StepQuizRunCodeView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        data class ConsequentLoading(val userCodeRun: UserCodeRun) : State()
        data class UserCodeRunLoaded(val userCodeRun: UserCodeRun) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
    fun showRunCodePopup()
}