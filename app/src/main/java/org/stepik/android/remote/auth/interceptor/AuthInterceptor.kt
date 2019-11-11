package org.stepik.android.remote.auth.interceptor

import android.content.Context
import com.facebook.login.LoginManager
import com.vk.sdk.VKSdk
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.stepic.droid.configuration.Config
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.StepikLogoutManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.addUserAgent
import org.stepic.droid.web.UserAgentProvider
import org.stepik.android.remote.auth.model.OAuthResponse
import org.stepik.android.remote.auth.service.OAuthService
import org.stepik.android.view.injection.qualifiers.AuthLock
import org.stepik.android.view.injection.qualifiers.AuthService
import org.stepik.android.view.injection.qualifiers.SocialAuthService
import retrofit2.Call
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class AuthInterceptor
@Inject
constructor(
    @AuthLock
    private val authLock: ReentrantLock,

    private val sharedPreference: SharedPreferenceHelper,
    private val stepikLogoutManager: StepikLogoutManager,
    private val screenManager: ScreenManager,
    private val config: Config,
    private val context: Context,
    private val userAgentProvider: UserAgentProvider,

    @SocialAuthService
    private val socialAuthService: OAuthService,

    @AuthService
    private val authService: OAuthService
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.addUserAgent(userAgentProvider.provideUserAgent())
        var response = addAuthHeaderAndProceed(chain, request)
        if (response.code() == 400) {
            // authPreferences.resetAuthResponseDeadline() - technically this is = val expiresMillis = (response.expiresIn - 50) * 1000
            response = addAuthHeaderAndProceed(chain, request)
        }
        return response
    }

    private fun addAuthHeaderAndProceed(chain: Interceptor.Chain, req: Request): Response {
        var request = req
        authLock.withLock {
            var response = sharedPreference.authResponseFromStore

            if (response != null) {
                if (isUpdateNeeded()) {
                    val oAuthResponse: retrofit2.Response<OAuthResponse>
                    try {
                        oAuthResponse = authWithRefreshToken(response.refreshToken).execute()
                        response = oAuthResponse.body()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        return chain.proceed(request)
                    }

                    if (response == null || !oAuthResponse.isSuccessful) {
                        if (oAuthResponse.code() == 401) {
                            stepikLogoutManager.logout {
                                LoginManager.getInstance().logOut()
                                VKSdk.logout()
                                screenManager.showLaunchScreen(context)
                            }
                        }
                        return chain.proceed(request)
                    }

                    sharedPreference.storeAuthInfo(response)
                }
                request = request.newBuilder()
                    .addHeader(AppConstants.authorizationHeaderName, response.tokenType + " " + response.accessToken)
                    .build()
            }
        }
        return chain.proceed(request)
    }

    private fun isUpdateNeeded(): Boolean {
        val response = sharedPreference.authResponseFromStore
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

    private fun authWithRefreshToken(refreshToken: String): Call<OAuthResponse> =
        (if (sharedPreference.isLastTokenSocial) socialAuthService else authService)
            .updateToken(config.refreshGrantType, refreshToken)
}