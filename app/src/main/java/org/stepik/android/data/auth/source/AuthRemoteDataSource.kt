package org.stepik.android.data.auth.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.social.SocialManager
import org.stepik.android.model.user.RegistrationCredentials
import org.stepik.android.remote.auth.model.OAuthResponse
import retrofit2.Call

interface AuthRemoteDataSource {
    fun authWithLoginPassword(login: String, password: String): Single<OAuthResponse>
    fun authWithNativeCode(code: String, type: SocialManager.SocialType, email: String?): Single<OAuthResponse>
    fun authWithCode(code: String): Single<OAuthResponse>
    fun createAccount(credentials: RegistrationCredentials): Completable
    fun remindPassword(email: String): Call<Void>
}