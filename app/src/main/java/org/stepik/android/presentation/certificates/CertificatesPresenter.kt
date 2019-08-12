package org.stepik.android.presentation.certificates

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.data.certificates.CertificatesInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class CertificatesPresenter
@Inject
constructor(
    private val certificatesInteractor: CertificatesInteractor,
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
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

    fun onLoadCertificates(userId: Long) {
        if (state != CertificatesView.State.Idle &&
            !(state == CertificatesView.State.NetworkError || state is CertificatesView.State.CertificatesLoaded)
        ) {
            return
        }

        state = CertificatesView.State.Loading

        compositeDisposable += certificatesInteractor
            .getCertificates(userId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onSuccess = {
                    state = if (it.isEmpty()) {
                        CertificatesView.State.EmptyCertificates
                    } else {
                        CertificatesView.State.CertificatesLoaded(it)
                    }
                },
                onError = {
                    state = CertificatesView.State.NetworkError
                    it.printStackTrace()
                }
            )
    }
}