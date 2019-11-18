package org.stepik.android.remote.auth.interceptor

import android.content.Context
import android.webkit.CookieManager
import com.facebook.login.LoginManager
import com.vk.sdk.VKSdk
import okhttp3.Interceptor
import okhttp3.Response
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.configuration.Config
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.StepikLogoutManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.addUserAgent
import org.stepic.droid.web.FailRefreshException
import org.stepic.droid.web.UserAgentProvider
import org.stepik.android.remote.auth.model.OAuthResponse
import org.stepik.android.remote.auth.service.EmptyAuthService
import org.stepik.android.remote.auth.service.OAuthService
import org.stepik.android.remote.base.CookieHelper
import org.stepik.android.view.injection.qualifiers.AuthLock
import org.stepik.android.view.injection.qualifiers.AuthService
import org.stepik.android.view.injection.qualifiers.SocialAuthService
import retrofit2.Call
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject

class AuthInterceptor
@Inject
constructor(
    @AuthLock
    private val authLock: ReentrantReadWriteLock,

    private val sharedPreference: SharedPreferenceHelper,
    private val stepikLogoutManager: StepikLogoutManager,
    private val screenManager: ScreenManager,
    private val config: Config,
    private val context: Context,
    private val userAgentProvider: UserAgentProvider,
    private val cookieHelper: CookieHelper,
    private val emptyAuthService: EmptyAuthService,
    private val analytic: Analytic,

    @SocialAuthService
    private val socialAuthService: OAuthService,

    @AuthService
    private val authService: OAuthService
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.addUserAgent(userAgentProvider.provideUserAgent())
        try {
            authLock.readLock().lock()
            var response = sharedPreference.authResponseFromStore
            val urlForCookies = request.url().toString()

            if (response == null) {
                // it is Anonymous, we can log it.
                val cookieManager = CookieManager.getInstance()
                var cookies = cookieManager.getCookie(config.baseUrl) // if token is expired or doesn't exist -> manager return null
                Timber.d("set cookie for url $urlForCookies is $cookies")

                if (cookies == null) {
                    cookieHelper.updateCookieForBaseUrl()
                    cookies = CookieManager.getInstance().getCookie(urlForCookies)
                }

                if (cookies != null) {
                    val csrfTokenFromCookies = cookieHelper.getCsrfTokenFromCookies(cookies)
                    if (sharedPreference.profile == null) {
                        val stepicProfileResponse = emptyAuthService.getUserProfileWithCookie(
                            config.baseUrl,
                            cookies,
                            csrfTokenFromCookies
                        ).execute().body()
                        if (stepicProfileResponse != null) {
                            val profile = stepicProfileResponse.getProfile()
                            sharedPreference.storeProfile(profile)
                        }
                    }

                    request = request
                        .newBuilder()
                        .addHeader(AppConstants.cookieHeaderName, cookies)
                        .addHeader(AppConstants.refererHeaderName, config.baseUrl)
                        .addHeader(AppConstants.csrfTokenHeaderName, csrfTokenFromCookies)
                        .build()
                }
            } else if (isUpdateNeeded(response)) {
                try {
                    authLock.readLock().unlock()
                    authLock.writeLock().lock()
                    Timber.d("writer 1")
                    response = sharedPreference.authResponseFromStore
                    if (isUpdateNeeded(response)) {
                        val oAuthResponse: retrofit2.Response<OAuthResponse>
                        try {
                            oAuthResponse = authWithRefreshToken(response!!.refreshToken).execute()
                            response = oAuthResponse.body()
                        } catch (e: IOException) {
                            return chain.proceed(request)
                        } catch (e: Exception) {
                            analytic.reportError(Analytic.Error.CANT_UPDATE_TOKEN, e)
                            return chain.proceed(request)
                        }
                        if (response == null || !oAuthResponse.isSuccessful) {
                            // it is worst case:
                            val message: String = response?.toString() ?: "response was null"
                            var extendedMessage = ""
                            if (oAuthResponse.isSuccessful) {
                                extendedMessage = "was success ${oAuthResponse.code()}"
                            } else {
                                try {
                                    extendedMessage = "failed ${oAuthResponse.code()} ${oAuthResponse.errorBody()?.string()}"
                                    if (oAuthResponse.code() == 401) {
                                        stepikLogoutManager.logout {
                                            LoginManager.getInstance().logOut()
                                            VKSdk.logout()
                                            screenManager.showLaunchScreen(context)
                                        }
                                    }
                                } catch (ex: Exception) {
                                    analytic.reportError(Analytic.Error.FAIL_REFRESH_TOKEN_INLINE_GETTING, ex)
                                }
                            }
                            analytic.reportError(Analytic.Error.FAIL_REFRESH_TOKEN_ONLINE_EXTENDED, FailRefreshException(extendedMessage))
                            analytic.reportError(Analytic.Error.FAIL_REFRESH_TOKEN_ONLINE, FailRefreshException(message))
                            analytic.reportEvent(Analytic.Web.UPDATE_TOKEN_FAILED)
                            return chain.proceed(request)
                        }
                        sharedPreference.storeAuthInfo(response)
                    }
                } finally {
                    authLock.readLock().lock()
                    Timber.d("writer 2")
                    authLock.writeLock().unlock()
                }
            }
            if (response != null) {
                request = request.newBuilder().addHeader(AppConstants.authorizationHeaderName, getAuthHeaderValueForLogged()).build()
            }
            val originalResponse = chain.proceed(request)
            val setCookieHeaders = originalResponse.headers(AppConstants.setCookieHeaderName)
            if (setCookieHeaders.isNotEmpty()) {
                for (value in setCookieHeaders) {
                    Timber.d("save for url $urlForCookies,  cookie $value")
                    if (value != null) {
                        CookieManager.getInstance().setCookie(urlForCookies, value) // set-cookie is not empty
                    }
                }
            }
            return originalResponse
        } finally {
            authLock.readLock().unlock()
        }
    }

    private fun isUpdateNeeded(response: OAuthResponse?): Boolean {
        if (response == null) {
            Timber.d("Token is null")
            return false
        }

        val timestampStored = sharedPreference.accessTokenTimestamp
        if (timestampStored == -1L) return true

        val nowTemp = DateTimeHelper.nowUtc()
        val delta = nowTemp - timestampStored
        val expiresMillis = (response.expiresIn - 50) * 1000
        return delta > expiresMillis // token expired --> need update
    }

    private fun getAuthHeaderValueForLogged(): String {
        val resp = sharedPreference.authResponseFromStore
            ?: return "" // not happen, look "resp null" in metrica before 07.2016

        val accessToken = resp.accessToken
        val type = resp.tokenType
        return "$type $accessToken"
    }

    private fun authWithRefreshToken(refreshToken: String): Call<OAuthResponse> =
        (if (sharedPreference.isLastTokenSocial) socialAuthService else authService)
            .updateToken(config.refreshGrantType, refreshToken)
}