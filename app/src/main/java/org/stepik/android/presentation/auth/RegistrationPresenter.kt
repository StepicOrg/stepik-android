package org.stepik.android.presentation.auth

import org.stepik.android.domain.auth.interactor.AuthInteractor
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.util.toObject
import org.stepik.android.domain.auth.model.RegistrationError
import org.stepik.android.model.user.RegistrationCredentials
import org.stepik.android.presentation.base.PresenterBase
import retrofit2.HttpException
import javax.inject.Inject

class RegistrationPresenter
@Inject
constructor(
    private val analytic: Analytic,
    private val authInteractor: AuthInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<RegistrationView>() {
    private var state: RegistrationView.State = RegistrationView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: RegistrationView) {
        super.attachView(view)
        view.setState(state)
    }

    fun onFormChanged() {
        state = RegistrationView.State.Idle
    }

    fun submit(registrationCredentials: RegistrationCredentials) {
        if (state == RegistrationView.State.Loading ||
            state is RegistrationView.State.Success) {
            return
        }

        state = RegistrationView.State.Loading
        compositeDisposable += authInteractor
            .createAccount(registrationCredentials)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { state = RegistrationView.State.Success(it) },
                onError = { throwable ->
                    val error = (throwable as? HttpException)
                        ?.response()
                        ?.errorBody()
                        ?.string()
                        ?.toObject<RegistrationError>()

                    if (throwable is HttpException) {
                        analytic.reportEvent(Analytic.Error.REGISTRATION_FAILED, throwable.response()?.errorBody()?.string() ?: "empty response")
                    } else {
                        analytic.reportError(Analytic.Error.REGISTRATION_FAILED, throwable)
                    }

                    state =
                        if (error != null) {
                            RegistrationView.State.Error(error)
                        } else {
                            view?.showNetworkError()
                            RegistrationView.State.Idle
                        }
                }
            )
    }
}