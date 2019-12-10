package org.stepik.android.presentation.profile_id

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfileIdPresenter
@Inject
constructor(
    profileDataObservable: Observable<ProfileData>,

    @BackgroundScheduler
    backgroundScheduler: Scheduler,
    @MainScheduler
    mainScheduler: Scheduler
) : PresenterBase<ProfileIdView>() {
    private var profileData: ProfileData? = null
        set(value) {
            field = value
            view?.setState(value)
        }

    init {
        compositeDisposable += profileDataObservable
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = ::profileData::set
            )
    }

    override fun attachView(view: ProfileIdView) {
        super.attachView(view)
        view.setState(profileData)
    }
}