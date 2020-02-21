package org.stepik.android.presentation.auth


interface LaunchView {
    sealed class State {
        object Idle : State()
        object Loading : State()

    }

    fun setState(state: State)
    fun showNetworkError()
}