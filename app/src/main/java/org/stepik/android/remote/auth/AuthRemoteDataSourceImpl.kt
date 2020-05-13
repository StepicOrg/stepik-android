package org.stepik.android.remote.auth

import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.stepic.droid.configuration.Config
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.addUserAgent
import org.stepic.droid.util.setTimeoutsInSeconds
import org.stepik.android.data.auth.source.AuthRemoteDataSource
import org.stepik.android.domain.auth.model.SocialAuthType
import org.stepik.android.model.user.RegistrationCredentials
import org.stepik.android.remote.auth.model.OAuthResponse
import org.stepik.android.remote.auth.model.TokenType
import org.stepik.android.remote.auth.model.UserRegistrationRequest
import org.stepik.android.remote.auth.service.EmptyAuthService
import org.stepik.android.remote.auth.service.OAuthService
import org.stepik.android.remote.base.CookieHelper
import org.stepik.android.remote.base.NetworkFactory
import org.stepik.android.remote.base.NetworkFactory.TIMEOUT_IN_SECONDS
import org.stepik.android.remote.base.UserAgentProvider
import org.stepik.android.view.injection.qualifiers.AuthLock
import org.stepik.android.view.injection.qualifiers.AuthService
import org.stepik.android.view.injection.qualifiers.CookieAuthService
import org.stepik.android.view.injection.qualifiers.SocialAuthService
import retrofit2.Converter
import retrofit2.Response
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

    private val userAgentProvider: UserAgentProvider,
    private val converterFactory: Converter.Factory,
    private val cookieHelper: CookieHelper,
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
            .authWithLoginPassword(config.getGrantType(TokenType.LOGIN_PASSWORD), URLEncoder.encode(login), URLEncoder.encode(password))
            .doOnSuccess { saveResponse(it, isSocial = false) }

    override fun authWithNativeCode(code: String, type: SocialAuthType, email: String?): Single<OAuthResponse> =
        socialAuthService
            .getTokenByNativeCode(
                type.identifier,
                code,
                config.getGrantType(TokenType.SOCIAL),
                config.redirectUri,
                if (type.isNeedUseAccessTokenInsteadOfCode) ACCESS_TOKEN else null,
                email
            )
            .doOnSuccess { saveResponse(it, isSocial = true) }

    override fun authWithCode(code: String): Single<OAuthResponse> =
        socialAuthService
            .getTokenByCode(config.getGrantType(TokenType.SOCIAL), code, config.redirectUri)
            .doOnSuccess { saveResponse(it, isSocial = true) }

    override fun createAccount(credentials: RegistrationCredentials): Completable =
        cookieAuthService.createAccount(
            UserRegistrationRequest(
                credentials
            )
        )

    override fun remindPassword(email: String): Single<Response<Void>> {
        val encodedEmail = URLEncoder.encode(email)

        val interceptor = Interceptor { chain ->
            var newRequest = chain.addUserAgent(userAgentProvider.provideUserAgent())

            val cookies =
                cookieHelper.getFreshCookiesForBaseUrl() ?: return@Interceptor chain.proceed(newRequest)

            var csrftoken: String? = null
            var sessionId: String? = null
            for (item in cookies) {
                if (item.name != null && item.name == config.csrfTokenCookieName) {
                    csrftoken = item.value
                    continue
                }
                if (item.name != null && item.name == config.sessionCookieName) {
                    sessionId = item.value
                }
            }

            val cookieResult =
                config.csrfTokenCookieName + "=" + csrftoken + "; " + config.sessionCookieName + "=" + sessionId
            if (csrftoken == null) return@Interceptor chain.proceed(newRequest)
            val url = newRequest
                .url()
                .newBuilder()
                .addQueryParameter("csrfmiddlewaretoken", csrftoken)
                .addQueryParameter("csrfmiddlewaretoken", csrftoken)
                .build()
            newRequest = newRequest.newBuilder()
                .addHeader("referer", config.baseUrl)
                .addHeader("X-CSRFToken", csrftoken)
                .addHeader("Cookie", cookieResult)
                .url(url)
                .build()
            chain.proceed(newRequest)
        }

        val okHttpBuilder = OkHttpClient.Builder()
        okHttpBuilder.addNetworkInterceptor(interceptor)
        okHttpBuilder.setTimeoutsInSeconds(TIMEOUT_IN_SECONDS)
        val notLogged =
            NetworkFactory.createRetrofit(config.baseUrl, okHttpBuilder.build(), converterFactory)
        val tempService = notLogged.create(EmptyAuthService::class.java)
        return tempService.remindPassword(encodedEmail)
    }
}