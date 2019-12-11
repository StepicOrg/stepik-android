package org.stepik.android.presentation.profile_courses

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

class ProfileCoursesPresenter
@Inject
constructor(
    private val profileDataObservable: Observable<ProfileData>,
    private val profileActivitiesInteractor: ProfileActivitiesInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileCoursesView>() {
    private var state: ProfileCoursesView.State = ProfileCoursesView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: ProfileCoursesView) {
        super.attachView(view)
        view.setState(state)
    }

    fun fetchUserActivities(forceUpdate: Boolean = false) {
        if (state == ProfileCoursesView.State.Idle || (forceUpdate && state == ProfileCoursesView.State.Error)) {
            state = ProfileCoursesView.State.SilentLoading
            compositeDisposable += profileDataObservable
                .firstElement()
                .filter { it.isCurrentUser && !it.user.isOrganization && !it.user.isPrivate }
                .observeOn(mainScheduler)
                .doOnSuccess { state = ProfileCoursesView.State.Loading } // post public loading to view
                .observeOn(backgroundScheduler)
                .flatMapSingleElement { profileData ->
                    profileActivitiesInteractor
                        .getProfileActivities(profileData.user.id)
                }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { state = ProfileCoursesView.State.Content(it) },
                    onComplete = { state = ProfileCoursesView.State.Empty },
                    onError = { state = ProfileCoursesView.State.Error }
                )
        }
    }
}