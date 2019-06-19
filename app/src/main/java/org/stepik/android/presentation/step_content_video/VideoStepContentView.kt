package org.stepik.android.presentation.step_content_video

interface VideoStepContentView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        class Loaded(val videoLength: String?) : State()
    }

    fun setState(state: State)
}