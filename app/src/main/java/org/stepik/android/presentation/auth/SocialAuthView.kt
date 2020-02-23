package org.stepik.android.presentation.auth

import org.stepik.android.domain.auth.model.LoginFailType

interface SocialAuthView {
    sealed class State {
        object Idle : State()
        object Loading : State()

        object Success : State()
    }

    fun setState(state: State)

    fun showAuthError(failType: LoginFailType)
    fun onSocialLoginWithExistingEmail(email: String)
    fun showNetworkError()
}