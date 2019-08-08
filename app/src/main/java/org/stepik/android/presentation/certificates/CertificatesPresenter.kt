package org.stepik.android.presentation.certificates

import io.reactivex.Scheduler
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class CertificatesPresenter
@Inject
constructor(
    @BackgroundScheduler
    backgroundScheduler: Scheduler,
    @MainScheduler
    mainScheduler: Scheduler
) : PresenterBase<CertificatesView>() {
    private var state: CertificatesView.State = CertificatesView.State.Idle
        set(value) {
            field = value
            view?.setState(state)
        }

    override fun attachView(view: CertificatesView) {
        super.attachView(view)
        view.setState(state)
    }
}