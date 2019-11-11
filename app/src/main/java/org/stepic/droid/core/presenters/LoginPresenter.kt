package org.stepic.droid.core.presenters

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.google.android.gms.auth.api.credentials.Credential
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.core.presenters.contracts.LoginView
import org.stepic.droid.di.login.LoginScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.model.Credentials
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.social.ISocialType
import org.stepic.droid.social.SocialManager
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.toObject
import org.stepic.droid.web.Api
import org.stepic.droid.web.SocialAuthError
import org.stepik.android.domain.auth.repository.AuthRepository
import org.stepik.android.model.user.RegistrationCredentials
import org.stepik.android.remote.auth.model.OAuthResponse
import retrofit2.HttpException
import javax.inject.Inject

@LoginScope
class LoginPresenter
@Inject constructor(
    private val api: Api,
    private val analytic: Analytic,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val authRepository: AuthRepository,

    @MainScheduler
    private val mainScheduler: Scheduler,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler
) : PresenterBase<LoginView>() {
    companion object {
        private const val MINUTES_TO_CONSIDER_REGISTRATION = 5
    }

    private val compositeDisposable = CompositeDisposable()

    private var authSocialType: ISocialType? = null

    fun onClickAuthWithSocialProviderWithoutSDK(type: ISocialType) {
        authSocialType = type
    }

    fun login(rawLogin: String, rawPassword: String, credential: Credential? = null, isAfterRegistration: Boolean = false) {
        val login = rawLogin.trim()
        doRequest(
            authRepository.authWithLoginPassword(login, rawPassword),
            AuthInfo(
                type = Type.LOGIN_PASSWORD,
                credentials = Credentials(login, rawPassword),
                credential = credential,
                isAfterRegistration = isAfterRegistration
            )
        )
    }

    fun loginWithCode(rawCode: String) {
        val code = rawCode.trim()
        doRequest(
            authRepository.authWithCode(code),
            AuthInfo(type = Type.SOCIAL, socialType = authSocialType)
        )
    }

    fun loginWithNativeProviderCode(nativeCode: String, type: SocialManager.SocialType, email: String? = null) {
        val code = nativeCode.trim()
        doRequest(
            authRepository.authWithNativeCode(code, type, email),
            AuthInfo(type = Type.SOCIAL, socialType = type)
        )
    }

    fun signUp(firstName: String, lastName: String, email: String, password: String) {
        compositeDisposable += authRepository.createAccount(RegistrationCredentials(firstName, lastName, email, password))
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = { login(email, password, isAfterRegistration = true)},
                onError = {
                    val responseBody = if (it is HttpException) {
                        it.response()?.errorBody()
                    } else {
                        null
                    }
                    view?.onRegistrationFailed(responseBody)
                }
            )
    }

    @MainThread
    private fun doRequest(source: Single<OAuthResponse>, authInfo: AuthInfo) {
        fun onFail(loginFailType: LoginFailType) {
            view?.onFailLogin(loginFailType, authInfo.credential)
        }

        view?.onLoadingWhileLogin()
        compositeDisposable += source
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { authResponse ->
                    sharedPreferenceHelper.storeAuthInfo(authResponse)
                    analytic.reportEvent(Analytic.Interaction.SUCCESS_LOGIN)
                    sharedPreferenceHelper.onSessionAfterLogin()

                    Completable.fromAction { resolveAmplitudeAuthAnalytic(authInfo) }
                    view?.onSuccessLogin(authInfo.credentials)

                },
                onError = {
                    if (it is HttpException) {
                        if (it.code() == 429) {
                            onFail(LoginFailType.TOO_MANY_ATTEMPTS)
                        } else if (it.code() == 401 && authInfo.type == Type.SOCIAL) {
                            val rawErrorMessage = it.response()?.errorBody()?.string()
                            val socialAuthError = rawErrorMessage?.toObject<SocialAuthError>()

                            val failType = when(socialAuthError?.error) {
                                AppConstants.ERROR_SOCIAL_AUTH_WITH_EXISTING_EMAIL -> {
                                    view?.onSocialLoginWithExistingEmail(socialAuthError.email ?: "")
                                    LoginFailType.EMAIL_ALREADY_USED
                                }

                                AppConstants.ERROR_SOCIAL_AUTH_WITHOUT_EMAIL -> LoginFailType.EMAIL_NOT_PROVIDED_BY_SOCIAL
                                else -> LoginFailType.UNKNOWN_ERROR
                            }

                            onFail(failType)
                        } else {
                            onFail(LoginFailType.EMAIL_PASSWORD_INVALID)
                        }
                    } else {
                        onFail(LoginFailType.CONNECTION_PROBLEM)
                    }
                }
            )
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

    override fun detachView(view: LoginView) {
        super.detachView(view)
        compositeDisposable.clear()
    }

    private enum class Type {
        SOCIAL, LOGIN_PASSWORD
    }

    private class AuthInfo(
            val isAfterRegistration: Boolean = false,
            val type: Type,
            val socialType: ISocialType? = null,
            val credentials: Credentials? = null,
            val credential: Credential? = null
    )
}
