package org.stepik.android.presentation.profile

import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.model.user.User


interface ProfileView {
    sealed class State {
        object Idle : State()
        object Loading : State()

        class Content(val profileData: ProfileData) : State()
        object Empty : State()
        object EmptyLogin : State()
        object NetworkError : State()
    }

    fun setState(state: State)

    fun shareUser(user: User)
    fun showNetworkError()
}