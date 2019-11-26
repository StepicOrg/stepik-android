package org.stepik.android.data.auth.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.social.SocialManager
import org.stepik.android.data.auth.source.AuthRemoteDataSource
import org.stepik.android.domain.auth.repository.AuthRepository
import org.stepik.android.model.user.RegistrationCredentials
import org.stepik.android.remote.auth.model.OAuthResponse
import retrofit2.Response
import javax.inject.Inject

@AppSingleton
class AuthRepositoryImpl
@Inject
constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override fun authWithLoginPassword(login: String, password: String): Single<OAuthResponse> =
        authRemoteDataSource.authWithLoginPassword(login, password)

    override fun authWithNativeCode(code: String, type: SocialManager.SocialType, email: String?): Single<OAuthResponse> =
        authRemoteDataSource.authWithNativeCode(code, type, email)

    override fun authWithCode(code: String): Single<OAuthResponse> =
        authRemoteDataSource.authWithCode(code)

    override fun createAccount(credentials: RegistrationCredentials): Completable =
        authRemoteDataSource.createAccount(credentials)

    override fun remindPassword(email: String): Single<Response<Void>> =
        authRemoteDataSource.remindPassword(email)
}