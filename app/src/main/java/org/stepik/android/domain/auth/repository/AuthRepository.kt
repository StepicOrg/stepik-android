package org.stepik.android.domain.auth.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.auth.model.SocialAuthType
import org.stepik.android.model.user.RegistrationCredentials
import org.stepik.android.remote.auth.model.OAuthResponse
import retrofit2.Response

interface AuthRepository {
    fun authWithLoginPassword(login: String, password: String): Single<OAuthResponse>
    fun authWithNativeCode(code: String, type: SocialAuthType, email: String? = null): Single<OAuthResponse>
    fun authWithCode(code: String): Single<OAuthResponse>
    fun createAccount(credentials: RegistrationCredentials): Completable
    fun remindPassword(email: String): Single<Response<Void>>
}