package org.stepik.android.presentation.profile_edit

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.profile_edit.ProfileEditInteractor
import org.stepik.android.model.user.Profile
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfileEditPresenter
@Inject
constructor(
    profileEditInteractor: ProfileEditInteractor,
    profileObservable: Observable<Profile>,

    @BackgroundScheduler
    backgroundScheduler: Scheduler,
    @MainScheduler
    mainScheduler: Scheduler
) : PresenterBase<ProfileEditView>() {
    private var state: ProfileEditView.State = ProfileEditView.State.Idle
        set(value) {
            field = value
            view?.setState(state)
        }

    init {
        compositeDisposable += profileEditInteractor
            .getProfileWithEmail()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { state = ProfileEditView.State.ProfileLoaded(it) },
                onError = { state = ProfileEditView.State.Error }
            )

        compositeDisposable += profileObservable
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onNext = {
                    state = ProfileEditView.State.ProfileLoaded(it)
                }
            )
    }

    override fun attachView(view: ProfileEditView) {
        super.attachView(view)
        view.setState(state)
    }
}