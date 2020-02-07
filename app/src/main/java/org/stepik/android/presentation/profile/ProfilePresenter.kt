package org.stepik.android.presentation.profile

import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.analytic.AmplitudeAnalytic
import org.stepic.droid.analytic.Analytic
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
    private val analytic: Analytic,

    private val profileObservable: Observable<Profile>,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileView>() {
    private var state: ProfileView.State = ProfileView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    private val profileUpdatesDisposable = CompositeDisposable()

    init {
        analytic.reportEvent(Analytic.Profile.OPEN_SCREEN_OVERALL)
        compositeDisposable += profileUpdatesDisposable
    }

    override fun attachView(view: ProfileView) {
        super.attachView(view)
        view.setState(state)
    }

    fun onData(userId: Long, forceUpdate: Boolean = false) {
        if (state != ProfileView.State.Idle && !(state == ProfileView.State.NetworkError && forceUpdate)) {
            return
        }

        state = ProfileView.State.Loading
        compositeDisposable += profileInteractor
            .getUser(userId)
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { profileData ->
                    state =
                        if (profileData.isCurrentUser && profileData.user.isGuest) {
                            ProfileView.State.EmptyLogin
                        } else {

                            ProfileView.State.Content(profileData)
                        }
                },
                onComplete = {
                    val oldState = state
                    if (oldState !is ProfileView.State.Content) {
                        state = ProfileView.State.Empty
                    } else {
                        subscribeForProfileUpdates(oldState.profileData.user.id)
                        sendScreenOpenEvent(oldState.profileData.user.id, oldState.profileData.isCurrentUser)
                    }
                },
                onError = {
                    val oldState = state
                    if (oldState !is ProfileView.State.Content) {
                        state = ProfileView.State.NetworkError
                    }
                }
            )
    }

    fun onShareProfileClicked() {
        val user = (state as? ProfileView.State.Content)
            ?.profileData
            ?.user
            ?: return
        view?.shareUser(user)
    }

    private fun sendScreenOpenEvent(userId: Long, isCurrentUser: Boolean) {
        val state = if (isCurrentUser) {
            AmplitudeAnalytic.Profile.Values.SELF
        } else {
            AmplitudeAnalytic.Profile.Values.OTHER
        }

        analytic.reportAmplitudeEvent(
            AmplitudeAnalytic.Profile.PROFILE_SCREEN_OPENED, mapOf(
                AmplitudeAnalytic.Profile.Params.STATE to state,
                AmplitudeAnalytic.Profile.Params.ID to userId
            ))
        analytic.reportEvent(Analytic.Profile.PROFILE_SCREEN_OPENED, Bundle().apply {
            putString(Analytic.Profile.Params.STATE, state)
        })
    }

    private fun subscribeForProfileUpdates(userId: Long) {
        profileUpdatesDisposable.clear()
        profileUpdatesDisposable += profileObservable
            .filter { it.id == userId }
            .observeOn(mainScheduler)
            .subscribeOn(backgroundScheduler)
            .subscribeBy(
                onNext = { onData(userId, forceUpdate = true) },
                onError = { subscribeForProfileUpdates(userId) }
            )
    }
}