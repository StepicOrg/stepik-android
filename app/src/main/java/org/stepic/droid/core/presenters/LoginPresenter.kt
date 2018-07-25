package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import com.google.android.gms.auth.api.credentials.Credential
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.core.presenters.contracts.LoginView
import org.stepic.droid.di.login.LoginScope
import org.stepic.droid.model.AuthData
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.social.ISocialType
import org.stepic.droid.social.SocialManager
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.toObject
import org.stepic.droid.web.Api
import org.stepic.droid.web.AuthenticationStepikResponse
import org.stepic.droid.web.SocialAuthError
import retrofit2.Call
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Inject

@LoginScope
class LoginPresenter
@Inject constructor(
        private val api: Api,
        private val analytic: Analytic,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val mainHandler: MainHandler
) : PresenterBase<LoginView>() {
    companion object {
        private const val MINUTES_TO_CONSIDER_REGISTRATION = 5
    }

    private var authSocialType: ISocialType? = null

    fun onClickAuthWithSocialProviderWithoutSDK(type: ISocialType) {
        authSocialType = type
    }

    fun login(rawLogin: String, rawPassword: String, credential: Credential? = null, isAfterRegistration: Boolean = false) {
        val login = rawLogin.trim()
        doRequest(api.authWithLoginPassword(login, rawPassword), AuthInfo(type = Type.LOGIN_PASSWORD, authData = AuthData(login, rawPassword), credential = credential, isAfterRegistration = isAfterRegistration))
    }

    fun loginWithCode(rawCode: String) {
        val code = rawCode.trim()
        doRequest(api.authWithCode(code), AuthInfo(type = Type.SOCIAL, socialType = authSocialType))
    }

    fun loginWithNativeProviderCode(nativeCode: String, type: SocialManager.SocialType, email: String? = null) {
        val code = nativeCode.trim()
        doRequest(api.authWithNativeCode(code, type, email), AuthInfo(type = Type.SOCIAL, socialType = type))
    }

    @MainThread
    private fun doRequest(callToServer: Call<AuthenticationStepikResponse>, authInfo: AuthInfo) {
        fun onFail(loginFailType: LoginFailType) {
            mainHandler.post {
                view?.onFailLogin(loginFailType, authInfo.credential)
            }
        }

        view?.onLoadingWhileLogin()
        threadPoolExecutor.execute {
            try {
                val response = callToServer.execute()
                if (response.isSuccessful) {
                    val authStepikResponse = response.body()
                    if (authStepikResponse != null) {
                        sharedPreferenceHelper.storeAuthInfo(authStepikResponse)
                        analytic.reportEvent(Analytic.Interaction.SUCCESS_LOGIN)
                        sharedPreferenceHelper.onSessionAfterLogin()

                        resolveAmplitudeAuthAnalytic(authInfo)
                        mainHandler.post { view?.onSuccessLogin(authInfo.authData) }
                    } else {
                        analytic.reportEvent(Analytic.Error.UNPREDICTABLE_LOGIN_RESULT)
                        //successful result, but body is not correct
                        onFail(LoginFailType.CONNECTION_PROBLEM)
                    }
                } else if (response.code() == 429) {
                    onFail(LoginFailType.TOO_MANY_ATTEMPTS)
                } else if (response.code() == 401 && authInfo.type == Type.SOCIAL) {
                    val rawErrorMessage = response.errorBody()?.string()
                    val socialAuthError = rawErrorMessage?.toObject<SocialAuthError>()

                    val failType = when(socialAuthError?.error) {
                        AppConstants.ERROR_SOCIAL_AUTH_WITH_EXISTING_EMAIL -> {
                            mainHandler.post {
                                view?.onSocialLoginWithExistingEmail(socialAuthError.email ?: "")
                            }
                            LoginFailType.EMAIL_ALREADY_USED
                        }

                        AppConstants.ERROR_SOCIAL_AUTH_WITHOUT_EMAIL -> LoginFailType.EMAIL_NOT_PROVIDED_BY_SOCIAL
                        else -> LoginFailType.UNKNOWN_ERROR
                    }

                    onFail(failType)
                } else {
                    onFail(LoginFailType.EMAIL_PASSWORD_INVALID)
                }
            } catch (ex: Exception) {
                onFail(LoginFailType.CONNECTION_PROBLEM)
            }
        }
    }

    @WorkerThread
    private fun resolveAmplitudeAuthAnalytic(authInfo: AuthInfo) {
        with(authInfo) {
            when(type) {
                Type.LOGIN_PASSWORD -> {
                    val event = if(isAfterRegistration) AmplitudeAnalytic.Auth.REGISTERED else AmplitudeAnalytic.Auth.LOGGED_ID
                    analytic.reportAmplitudeEvent(event, mapOf(AmplitudeAnalytic.Auth.PARAM_SOURCE to AmplitudeAnalytic.Auth.VALUE_SOURCE_EMAIL))
                }

                Type.SOCIAL -> {
                    if (authInfo.socialType == null) return

                    val event: String = try {
                        val request = api.userProfile.execute().body()

                        val user = request?.getUser()
                        val profile = request?.getProfile()

                        if (profile != null) {
                            sharedPreferenceHelper.storeProfile(profile)
                        }

                        user?.joinDate?.let {
                            if (DateTimeHelper.nowUtc() - it.time < MINUTES_TO_CONSIDER_REGISTRATION * AppConstants.MILLIS_IN_1MINUTE) {
                                AmplitudeAnalytic.Auth.REGISTERED
                            } else {
                                AmplitudeAnalytic.Auth.LOGGED_ID
                            }
                        } ?: AmplitudeAnalytic.Auth.LOGGED_ID
                    } catch (_: Exception) {
                        AmplitudeAnalytic.Auth.LOGGED_ID
                    }

                    analytic.reportAmplitudeEvent(event, mapOf(AmplitudeAnalytic.Auth.PARAM_SOURCE to authInfo.socialType.identifier))
                }
            }
        }
    }

    private enum class Type {
        SOCIAL, LOGIN_PASSWORD
    }

    private class AuthInfo(
            val isAfterRegistration: Boolean = false,
            val type: Type,
            val socialType: ISocialType? = null,
            val authData: AuthData? = null,
            val credential: Credential? = null
    )
}
