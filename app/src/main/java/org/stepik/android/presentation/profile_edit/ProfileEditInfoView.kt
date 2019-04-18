package org.stepik.android.presentation.profile_edit

interface ProfileEditInfoView {
    enum class State {
        IDLE, LOADING, COMPLETE
    }

    fun setState(state: State)
    fun showInfoError()
    fun showNetworkError()
}