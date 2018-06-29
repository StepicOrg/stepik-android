package org.stepic.droid.core.presenters

import android.support.annotation.MainThread
import com.google.android.gms.auth.api.credentials.Credential
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.core.presenters.contracts.LoginView
import org.stepic.droid.di.login.LoginScope
import org.stepic.droid.model.AuthData
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.social.SocialManager
import org.stepic.droid.util.AppConstants
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
        private val mainHandler: MainHandler) : PresenterBase<LoginView>() {

    fun login(rawLogin: String, rawPassword: String, credential: Credential? = null) {
        val login = rawLogin.trim()
        doRequest(api.authWithLoginPassword(login, rawPassword), AuthData(login, rawPassword), Type.loginPassword, credential)
    }

    fun loginWithCode(rawCode: String) {
        val code = rawCode.trim()
        doRequest(api.authWithCode(code), null, Type.social)
    }

    fun loginWithNativeProviderCode(nativeCode: String, type: SocialManager.SocialType, email: String? = null) {
        val code = nativeCode.trim()
        doRequest(api.authWithNativeCode(code, type, email),
                null,
                Type.social)
    }

    @MainThread
    private fun doRequest(callToServer: Call<AuthenticationStepikResponse>, authData: AuthData?, type: Type, credential: Credential? = null) {
        fun onFail(loginFailType: LoginFailType, description: String? = null) {
            analytic.reportEventWithName(Analytic.Login.FAIL_LOGIN, loginFailType.toString() + if (description != null) ": $description" else "")
            mainHandler.post {
                view?.onFailLogin(loginFailType, credential)
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

                        mainHandler.post { view?.onSuccessLogin(authData) }
                    } else {
                        analytic.reportEvent(Analytic.Error.UNPREDICTABLE_LOGIN_RESULT)
                        //successful result, but body is not correct
                        onFail(LoginFailType.CONNECTION_PROBLEM)
                    }
                } else if (response.code() == 429) {
                    onFail(LoginFailType.TOO_MANY_ATTEMPTS)
                } else if (response.code() == 401 && type == Type.social) {
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

    private enum class Type {
        social, loginPassword
    }

}
