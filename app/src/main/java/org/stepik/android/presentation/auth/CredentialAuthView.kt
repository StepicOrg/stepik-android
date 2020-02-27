package org.stepik.android.presentation.auth

import org.stepik.android.domain.auth.model.LoginFailType
import org.stepic.droid.model.Credentials

interface CredentialAuthView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        class Success(val credentials: Credentials?) : State()
        class Error(val failType: LoginFailType) : State()
    }

    fun setState(state: State)
}