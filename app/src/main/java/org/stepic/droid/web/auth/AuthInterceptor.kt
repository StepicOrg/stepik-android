//package org.stepic.droid.web.auth
//
//import android.content.Context
//import android.webkit.CookieManager
//import android.widget.Toast
//import com.facebook.login.LoginManager
//import com.vk.sdk.VKSdk
//import okhttp3.Interceptor
//import okhttp3.Response
//import org.stepic.droid.R
//import org.stepic.droid.analytic.Analytic
//import org.stepic.droid.configuration.Config
//import org.stepic.droid.core.StepikLogoutManager
//import org.stepic.droid.di.AppSingleton
//import org.stepic.droid.preferences.SharedPreferenceHelper
//import org.stepic.droid.util.*
//import org.stepic.droid.web.AuthenticationStepikResponse
//import org.stepic.droid.web.FailRefreshException
//import timber.log.Timber
//import java.io.IOException
//import javax.inject.Inject
//import javax.inject.Named
//
//@AppSingleton
//class AuthInterceptor
//@Inject
//constructor(
//        @Named(AppConstants.userAgentName)
//        private val userAgent: String,
//
//        private val context: Context,
//
//        @AuthService
//        private val authService: StepikRest,
//        @SocialAuthService
//        private val socialAuthService: OAuthService,
//
//        private val sharedPreference: SharedPreferenceHelper,
//        private val config: Config,
//        private val analytic: Analytic,
//        private val cookieHelper: CookieHelper,
//        private val stepikLogoutManager: StepikLogoutManager
//): Interceptor {
//    override fun intercept(chain: Interceptor.Chain): Response {
//        var newRequest = chain.addUserAgent(userAgent)
//        try {
//            RWLocks.AuthLock.readLock().lock()
//            var response = sharedPreference.authResponseFromStore
//            val urlForCookies = newRequest.url().toString()
//            if (response == null) {
//                //it is Anonymous, we can log it.
//
//                cookieHelper.updateCookieForBaseUrl()
//                newRequest = cookieHelper.addCsrfTokenToRequest(newRequest)
//                val cookieManager = android.webkit.CookieManager.getInstance()
//                var cookies: String? = cookieManager.getCookie(config.baseUrl) //if token is expired or doesn't exist -> manager return null
//                Timber.d("set cookie for url %s is %s", urlForCookies, cookies)
//                if (cookies == null) {
//                    cookieHelper.updateCookieForBaseUrl()
//                    cookies = android.webkit.CookieManager.getInstance().getCookie(urlForCookies)
//                }
//                if (cookies != null) {
//                    val csrfTokenFromCookies = getCsrfTokenFromCookies(cookies)
//                    if (sharedPreference.profile == null) {
//                        val stepicProfileResponse = stepikEmptyAuthService.getUserProfileWithCookie(config.baseUrl, cookies, csrfTokenFromCookies).execute().body()
//                        if (stepicProfileResponse != null) {
//                            val profile = stepicProfileResponse.getProfile()
//                            sharedPreference.storeProfile(profile)
//                        }
//                    }
//                    newRequest = newRequest
//                            .newBuilder()
//                            .addHeader(AppConstants.cookieHeaderName, cookies)
//                            .addHeader(AppConstants.refererHeaderName, config.baseUrl)
//                            .addHeader(AppConstants.csrfTokenHeaderName, csrfTokenFromCookies)
//                            .build()
//                }
//            } else if (isNeededUpdate(response)) {
//                try {
//                    RWLocks.AuthLock.readLock().unlock()
//                    RWLocks.AuthLock.writeLock().lock()
//                    Timber.d("writer 1")
//                    response = sharedPreference.authResponseFromStore
//                    if (isNeededUpdate(response)) {
//                        val authenticationStepicResponse: retrofit2.Response<AuthenticationStepikResponse>
//                        try {
//                            authenticationStepicResponse = oAuthService.updateToken(config.refreshGrantType, response!!.refreshToken).execute()
//                            response = authenticationStepicResponse.body()
//                        } catch (e: IOException) {
//                            return chain.proceed(newRequest)
//                        } catch (e: Exception) {
//                            analytic.reportError(Analytic.Error.CANT_UPDATE_TOKEN, e)
//                            return chain.proceed(newRequest)
//                        }
//
//                        if (response == null || !response.isSuccess()) {
//                            //it is worst case:
//                            val message: String
//                            if (response == null) {
//                                message = "response was null"
//                            } else {
//                                message = response.toString()
//                            }
//
//                            var extendedMessage = ""
//                            if (authenticationStepicResponse.isSuccessful) {
//                                extendedMessage = "was success " + authenticationStepicResponse.code()
//                            } else {
//                                try {
//                                    extendedMessage = "failed " + authenticationStepicResponse.code() + " " + authenticationStepicResponse.errorBody()!!.string()
//                                    if (authenticationStepicResponse.code() == 401) {
//                                        // logout user
//                                        stepikLogoutManager.logout {
//                                                    try {
//                                                        LoginManager.getInstance().logOut()
//                                                        VKSdk.logout()
//                                                    } catch (e: Exception) {
//                                                        analytic.reportError(Analytic.Error.FAIL_LOGOUT_WHEN_REFRESH, e)
//                                                    }
//
//                                                    screenManager.showLaunchScreenAfterLogout(context)
//                                                    Toast.makeText(context, R.string.logout_user_error, Toast.LENGTH_SHORT).show()
//                                                }
//
//                                    }
//
//                                } catch (ex: Exception) {
//                                    analytic.reportError(Analytic.Error.FAIL_REFRESH_TOKEN_INLINE_GETTING, ex)
//                                }
//
//                            }
//                            analytic.reportError(Analytic.Error.FAIL_REFRESH_TOKEN_ONLINE_EXTENDED, FailRefreshException(extendedMessage))
//                            analytic.reportError(Analytic.Error.FAIL_REFRESH_TOKEN_ONLINE, FailRefreshException(message))
//                            analytic.reportEvent(Analytic.Web.UPDATE_TOKEN_FAILED)
//                            return chain.proceed(newRequest)
//                        }
//
//                        //Update is success:
//                        sharedPreference.storeAuthInfo(response)
//                    }
//                } finally {
//                    RWLocks.AuthLock.readLock().lock()
//                    Timber.d("writer 2")
//                    RWLocks.AuthLock.writeLock().unlock()
//                }
//            }
//            if (response != null) {
//                //it is good way
//                newRequest = newRequest.newBuilder().addHeader(AppConstants.authorizationHeaderName, getAuthHeaderValueForLogged()).build()
//            }
//            val originalResponse = chain.proceed(newRequest)
//            val setCookieHeaders = originalResponse.headers(AppConstants.setCookieHeaderName)
//            if (!setCookieHeaders.isEmpty()) {
//                for (value in setCookieHeaders) {
//                    Timber.d("save for url %s,  cookie %s", urlForCookies, value)
//                    if (value != null) {
//                        CookieManager.getInstance().setCookie(urlForCookies, value) //set-cookie is not empty
//                    }
//                }
//            }
//            return originalResponse
//        } finally {
//            RWLocks.AuthLock.readLock().unlock()
//        }
//
//    }
//
//    private fun isNeededUpdate(response: AuthenticationStepikResponse?): Boolean {
//        if (response == null) {
//            Timber.d("Token is null")
//            return false
//        }
//
//        val timestampStored = sharedPreference.accessTokenTimestamp
//        if (timestampStored == -1L) return true
//
//        val nowTemp = DateTimeHelper.nowUtc()
//        val delta = nowTemp - timestampStored
//        val expiresMillis = (response.expiresIn - 50) * 1000
//        return delta > expiresMillis//token expired --> need update
//    }
//}
