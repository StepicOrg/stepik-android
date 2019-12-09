package org.stepik.android.presentation.profile_achievements

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.achievement.interactor.AchievementInteractor
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.presentation.achievement.AchievementsView
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class ProfileAchievementsPresenter
@Inject
constructor(
    private val profileDataObservable: Observable<ProfileData>,
    private val achievementInteractor: AchievementInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
) : PresenterBase<AchievementsView>() {
    private var state: AchievementsView.State = AchievementsView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: AchievementsView) {
        super.attachView(view)
        view.setState(state)
    }

    fun showAchievementsForUser(count: Int = -1, forceUpdate: Boolean = false) {
        if (state == AchievementsView.State.Idle || (forceUpdate && state == AchievementsView.State.Error)) {
            state = AchievementsView.State.SilentLoading
            compositeDisposable += profileDataObservable
                .firstElement()
                .filter { !it.user.isOrganization && !it.user.isPrivate }
                .observeOn(mainScheduler)
                .doOnSuccess { state = AchievementsView.State.Loading } // post public loading to view
                .observeOn(backgroundScheduler)
                .flatMapSingleElement { profileData ->
                    achievementInteractor
                        .getAchievements(profileData.user.id, count)
                        .map { it to profileData.isCurrentUser }
                }
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { (achievements, isMyProfile) ->
                        state = AchievementsView.State.AchievementsLoaded(achievements, isMyProfile)
                    },
                    onComplete = { state = AchievementsView.State.NoAchievements },
                    onError = { state = AchievementsView.State.Error }
                )
        }
    }
}