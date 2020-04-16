package org.stepik.android.presentation.auth

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.model.Credentials
import org.stepic.droid.util.emptyOnErrorStub
import org.stepik.android.domain.auth.interactor.AuthInteractor
import org.stepik.android.domain.auth.model.LoginFailType
import org.stepik.android.presentation.base.PresenterBase
import retrofit2.HttpException
import javax.inject.Inject

class CredentialAuthPresenter
@Inject
constructor(
    private val authInteractor: AuthInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<CredentialAuthView>() {
    private var state: CredentialAuthView.State = CredentialAuthView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: CredentialAuthView) {
        super.attachView(view)
        view.setState(state)
    }

    fun onFormChanged() {
        state = CredentialAuthView.State.Idle
    }

    fun submit(credentials: Credentials, isRegistration: Boolean = false) {
        if (state == CredentialAuthView.State.Loading ||
            state is CredentialAuthView.State.Success) {
            return
        }

        state = CredentialAuthView.State.Loading
        compositeDisposable += authInteractor
            .authWithCredentials(credentials, isRegistration)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { clearCoursesBeforeAuth(it) },
                onError = { throwable ->
                    val loginFailType =
                        if (throwable is HttpException) {
                            if (throwable.code() == 429) {
                                LoginFailType.TOO_MANY_ATTEMPTS
                            } else {
                                LoginFailType.EMAIL_PASSWORD_INVALID
                            }
                        } else {
                            LoginFailType.CONNECTION_PROBLEM
                        }
                    state = CredentialAuthView.State.Error(loginFailType)
                }
            )
    }

    private fun clearCoursesBeforeAuth(credentials: Credentials) {
        compositeDisposable += authInteractor.clearCourseRepository()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onComplete = { state = CredentialAuthView.State.Success(credentials) },
                onError = emptyOnErrorStub
            )
    }
}