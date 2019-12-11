package org.stepik.android.presentation.profile_certificates

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.certificate.interactor.CertificatesInteractor
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfileCertificatesPresenter
@Inject
constructor(
    private val profileDataObservable: Observable<ProfileData>,
    private val certificatesInteractor: CertificatesInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileCertificatesView>() {
    private var state: ProfileCertificatesView.State = ProfileCertificatesView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: ProfileCertificatesView) {
        super.attachView(view)
        view.setState(state)
    }

    fun showCertificatesForUser(forceUpdate: Boolean = false) {
        if (state == ProfileCertificatesView.State.Idle || (forceUpdate && state == ProfileCertificatesView.State.Error)) {
            state = ProfileCertificatesView.State.SilentLoading
            compositeDisposable += profileDataObservable
                .firstElement()
                .filter { !it.user.isPrivate }
                .observeOn(mainScheduler)
                .doOnSuccess { profileData ->
                    state = ProfileCertificatesView.State.Loading(profileData.user.id)
                } // post public loading to view
                .observeOn(backgroundScheduler)
                .flatMapSingleElement { profileData ->
                     certificatesInteractor
                         .getCertificates(profileData.user.id)
                         .map { it to profileData.user.id }
                }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { (certificates, userId) ->
                        state = ProfileCertificatesView.State.CertificatesLoaded(certificates, userId)
                    },
                    onComplete = { state = ProfileCertificatesView.State.NoCertificates},
                    onError = { state = ProfileCertificatesView.State.Error }
                )
        }
    }
}