package org.stepik.android.presentation.step_quiz_code

import org.stepik.android.model.code.UserCodeRun

interface StepQuizRunCode {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object Empty : State()
        data class UserCodeRunLoaded(val userCodeRun: UserCodeRun) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}