package org.stepik.android.presentation.auth

import org.stepic.droid.model.Credentials
import org.stepik.android.domain.auth.model.RegistrationError


interface RegistrationView {
    sealed class State {
        object Idle : State()
        object Loading : State()
        class Success(val credentials: Credentials) : State()
        class Error(val data: RegistrationError) : State()
    }

    fun setState(state: State)
    fun showNetworkError()
}