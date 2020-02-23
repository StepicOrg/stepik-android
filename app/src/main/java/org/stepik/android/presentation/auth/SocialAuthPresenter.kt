package org.stepik.android.presentation.auth

import io.reactivex.Completable
import org.stepik.android.domain.auth.interactor.AuthInteractor
import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.social.ISocialType
import org.stepik.android.presentation.base.PresenterBase
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

    }
}