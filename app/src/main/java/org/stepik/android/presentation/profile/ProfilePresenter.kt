package org.stepik.android.presentation.profile

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.profile.interactor.ProfileInteractor
import org.stepik.android.model.user.Profile
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfilePresenter
@Inject
constructor(
    private val profileInteractor: ProfileInteractor,
    private val profileObservable: Observable<Profile>,

    @MainScheduler
    private val mainScheduler: Scheduler,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler
) : PresenterBase<ProfileView>() {

    private fun showLocalProfile(profile: Profile) {

    }

    private fun subscribeForProfileUpdates(profileId: Long) {
        compositeDisposable += profileObservable
            .filter { profileId == 0L || it.id == profileId }
            .observeOn(backgroundScheduler)
            .subscribe(::showLocalProfile)
    }
}