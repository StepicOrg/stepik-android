package org.stepik.android.presentation.profile

import org.stepik.android.model.user.User


interface ProfileView {
    sealed class State {
        object Idle : State()
        object Loading : State()

        class Content(val user: User) : State()
        object Empty : State()
        object NetworkError : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}