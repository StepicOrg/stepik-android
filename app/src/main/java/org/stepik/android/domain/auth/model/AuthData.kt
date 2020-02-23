package org.stepik.android.domain.auth.model

import com.google.android.gms.auth.api.credentials.Credential
import org.stepic.droid.social.ISocialType

sealed class AuthData {
    data class Credentials(
        val email: String,
        val password: String,
        val isRegistration: Boolean,
        val smartLockCredential: Credential? = null
    ) : AuthData()

    data class Social(
        val type: ISocialType
    ) : AuthData()
}