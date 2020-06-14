package org.stepik.android.presentation.profile_activities

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.domain.profile_activities.interactor.ProfileActivitiesInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfileActivitiesPresenter
@Inject
constructor(
    private val profileDataObservable: Observable<ProfileData>,
    private val profileActivitiesInteractor: ProfileActivitiesInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileActivitiesView>() {
    private var state: ProfileActivitiesView.State = ProfileActivitiesView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: ProfileActivitiesView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchUserActivities(forceUpdate: Boolean = false) {
        if (state == ProfileActivitiesView.State.Idle || (forceUpdate && state == ProfileActivitiesView.State.Error)) {
            state = ProfileActivitiesView.State.SilentLoading
            compositeDisposable += profileDataObservable
                .firstElement()
                .filter { it.isCurrentUser && !it.user.isOrganization && !it.user.isPrivate }
                .observeOn(mainScheduler)
                .doOnSuccess { state = ProfileActivitiesView.State.Loading } // post public loading to view
                .observeOn(backgroundScheduler)
                .flatMapSingleElement { profileData ->
                    profileActivitiesInteractor
                        .getProfileActivities(profileData.user.id)
                }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { state = ProfileActivitiesView.State.Content(it) },
                    onComplete = { state = ProfileActivitiesView.State.Empty },
                    onError = { state = ProfileActivitiesView.State.Error }
                )
        }
    }
}