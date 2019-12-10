package org.stepik.android.presentation.profile_links

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.domain.social_profile.interactor.SocialProfileInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfileLinksPresenter
@Inject
constructor(
    private val profileDataObservable: Observable<ProfileData>,
    private val socialProfileInteractor: SocialProfileInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<ProfileLinksView>() {
    private var state: ProfileLinksView.State = ProfileLinksView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: ProfileLinksView) {
        super.attachView(view)
        view.setState(state)
    }

    fun showSocialProfiles(forceUpdate: Boolean = false) {
        if (state == ProfileLinksView.State.Idle || (forceUpdate && state == ProfileLinksView.State.Error)) {
            state = ProfileLinksView.State.Loading
            compositeDisposable += profileDataObservable
                .firstElement()
                .filter { !it.user.isPrivate }
                .flatMapSingleElement { profileData ->
                    socialProfileInteractor
                        .getSocialProfiles(*profileData.user.socialProfiles.toLongArray())
                }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = {
                        state = if (it.isNotEmpty()) {
                            ProfileLinksView.State.ProfileLinksLoaded(it)
                        } else {
                            ProfileLinksView.State.Empty
                        }
                    },
                    onComplete = {
                        state = ProfileLinksView.State.Empty
                    },
                    onError = {
                        state = ProfileLinksView.State.Error
                    }
                )
        }
    }
}