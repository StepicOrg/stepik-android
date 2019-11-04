package org.stepic.droid.features.auth.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.social.SocialManager
import org.stepic.droid.web.OAuthResponse
import org.stepik.android.model.user.RegistrationCredentials

interface AuthRepository {
    fun authWithLoginPassword(login: String, password: String): Single<OAuthResponse>
    fun authWithNativeCode(code: String, type: SocialManager.SocialType, email: String? = null): Single<OAuthResponse>
    fun authWithCode(code: String): Single<OAuthResponse>

    fun createAccount(credentials: RegistrationCredentials): Completable
}