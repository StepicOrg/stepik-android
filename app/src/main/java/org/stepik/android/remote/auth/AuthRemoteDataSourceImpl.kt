package org.stepik.android.remote.auth

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.configuration.Config
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.social.SocialManager
import org.stepic.droid.web.Api
import org.stepik.android.remote.auth.model.UserRegistrationRequest
import org.stepik.android.data.auth.source.AuthRemoteDataSource
import org.stepik.android.model.user.RegistrationCredentials
import org.stepik.android.remote.auth.model.OAuthResponse
import org.stepik.android.remote.auth.service.OAuthService
import org.stepik.android.view.injection.qualifiers.AuthLock
import org.stepik.android.view.injection.qualifiers.AuthService
import org.stepik.android.view.injection.qualifiers.CookieAuthService
import org.stepik.android.view.injection.qualifiers.SocialAuthService
import java.net.URLEncoder
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.write

class AuthRemoteDataSourceImpl
@Inject
constructor(
    @AuthLock
    private val authLock: ReentrantReadWriteLock,

    @AuthService
    private val authService: OAuthService,
    @SocialAuthService
    private val socialAuthService: OAuthService,
    @CookieAuthService
    private val cookieAuthService: OAuthService,

    private val config: Config,

    private val sharedPreferenceHelper: SharedPreferenceHelper
) : AuthRemoteDataSource {
    companion object {
        private const val ACCESS_TOKEN = "access_token"
    }

    private fun saveResponse(response: OAuthResponse, isSocial: Boolean) {
        authLock.write {
            sharedPreferenceHelper.storeAuthInfo(response)
            sharedPreferenceHelper.storeLastTokenType(isSocial)
        }
    }

    override fun authWithLoginPassword(login: String, password: String): Single<OAuthResponse> =
        authService
            .authWithLoginPassword(config.getGrantType(Api.TokenType.loginPassword), URLEncoder.encode(login), URLEncoder.encode(password))
            .doOnSuccess { saveResponse(it, isSocial = false) }

    override fun authWithNativeCode(code: String, type: SocialManager.SocialType, email: String?): Single<OAuthResponse> =
        socialAuthService
            .getTokenByNativeCode(
                type.identifier,
                code,
                config.getGrantType(Api.TokenType.social),
                config.redirectUri,
                if (type.needUseAccessTokenInsteadOfCode()) ACCESS_TOKEN else null,
                email
            )
            .doOnSuccess { saveResponse(it, isSocial = true) }

    override fun authWithCode(code: String): Single<OAuthResponse> =
        socialAuthService
            .getTokenByCode(config.getGrantType(Api.TokenType.social), code, config.redirectUri)
            .doOnSuccess { saveResponse(it, isSocial = true) }

    override fun createAccount(credentials: RegistrationCredentials): Completable =
        cookieAuthService.createAccount(
            UserRegistrationRequest(
                credentials
            )
        )
}