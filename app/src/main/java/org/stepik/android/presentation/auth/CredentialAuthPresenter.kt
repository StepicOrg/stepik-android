package org.stepik.android.presentation.auth

import org.stepik.android.domain.auth.interactor.AuthInteractor
import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.presentation.base.PresenterBase
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
}