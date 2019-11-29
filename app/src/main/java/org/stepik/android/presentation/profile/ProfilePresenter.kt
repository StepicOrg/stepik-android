package org.stepik.android.presentation.profile

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.profile.interactor.ProfileInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfilePresenter
@Inject
constructor(
    private val profileInteractor: ProfileInteractor,

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
}