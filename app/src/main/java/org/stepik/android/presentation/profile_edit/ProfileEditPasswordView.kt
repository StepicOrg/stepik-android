package org.stepik.android.presentation.profile_edit

interface ProfileEditPasswordView {
    enum class State {
        IDLE, LOADING, COMPLETE
    }

    fun setState(state: State)
    fun showPasswordError()
    fun showNetworkError()
}