package org.stepik.android.presentation.step_source

import org.stepik.android.model.Step

interface EditStepSourceView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        class Complete(val step: Step) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}