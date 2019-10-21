package org.stepik.android.presentation.step_edit

import org.stepik.android.model.Step

interface EditStepContentView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        class Complete(val step: Step) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}