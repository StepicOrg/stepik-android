package org.stepik.android.presentation.in_app_web_view

interface InAppWebViewView {
    sealed class State {
        object Idle : State()
        object LinkLoading : State()
        class WebLoading(val url: String) : State()
        object Error : State()
        object Success : State()
    }

    fun setState(state: State)
}