package org.stepik.android.presentation.step_source

import org.stepic.droid.persistence.model.StepPersistentWrapper

interface EditStepSourceView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        object StepLoaded : State()
        class Complete(val stepWrapper: StepPersistentWrapper) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
    fun setStepWrapperInfo(stepWrapper: StepPersistentWrapper)
}