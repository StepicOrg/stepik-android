package org.stepik.android.domain.auth.model

import org.stepic.droid.social.ISocialType

sealed class AuthData {
    data class Credentials(
        val email: String,
        val password: String,
        val isRegistration: Boolean
    ) : AuthData()

    data class Social(
        val type: ISocialType
    ) : AuthData()
}