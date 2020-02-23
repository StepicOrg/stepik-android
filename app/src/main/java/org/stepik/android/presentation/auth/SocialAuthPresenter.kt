package org.stepik.android.presentation.auth

import io.reactivex.Completable
import org.stepik.android.domain.auth.interactor.AuthInteractor
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.core.LoginFailType
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.social.ISocialType
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.toObject
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.domain.auth.model.SocialAuthError
import retrofit2.HttpException
import javax.inject.Inject

class SocialAuthPresenter
@Inject
constructor(
    private val authInteractor: AuthInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<SocialAuthView>() {
    private var state: SocialAuthView.State = SocialAuthView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: SocialAuthView) {
        super.attachView(view)
        view.setState(state)
    }

    fun authWithNativeCode(code: String, type: ISocialType, email: String? = null) {
        auth(authInteractor.authWithNativeCode(code, type, email))
    }

    fun authWithCode(code: String, type: ISocialType) {
        auth(authInteractor.authWithCode(code, type))
    }

    private fun auth(authSource: Completable) {
        if (state != SocialAuthView.State.Idle) return

        state = SocialAuthView.State.Loading

        compositeDisposable += authSource
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = { state = SocialAuthView.State.Success },
                onError = { throwable ->
                    val failType =
                        when ((throwable as? HttpException)?.code()) {
                            429 -> LoginFailType.TOO_MANY_ATTEMPTS
                            401 -> {
                                val rawErrorMessage = throwable.response()?.errorBody()?.string()
                                val socialAuthError = rawErrorMessage?.toObject<SocialAuthError>()

                                when (socialAuthError?.error) {
                                    AppConstants.ERROR_SOCIAL_AUTH_WITH_EXISTING_EMAIL -> {
                                        view?.onSocialLoginWithExistingEmail(socialAuthError.email ?: "")
                                        LoginFailType.EMAIL_ALREADY_USED
                                    }

                                    AppConstants.ERROR_SOCIAL_AUTH_WITHOUT_EMAIL ->
                                        LoginFailType.EMAIL_NOT_PROVIDED_BY_SOCIAL

                                    else ->
                                        LoginFailType.UNKNOWN_ERROR
                                }
                            }
                            else ->
                                LoginFailType.CONNECTION_PROBLEM
                        }

                    view?.showAuthError(failType)

                    state = SocialAuthView.State.Idle
                }
            )
    }
}