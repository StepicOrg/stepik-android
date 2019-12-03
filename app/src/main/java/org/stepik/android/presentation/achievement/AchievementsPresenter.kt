package org.stepik.android.presentation.achievement

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.achievement.interactor.AchievementInteractor
import org.stepik.android.presentation.base.PresenterBase
import org.stepik.android.presentation.profile.ProfileView
import javax.inject.Inject

class AchievementsPresenter
@Inject
constructor(
    private val achievementInteractor: AchievementInteractor,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
): PresenterBase<AchievementsView>() {
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
            state = AchievementsView.State.Loading
            compositeDisposable += achievementInteractor
                .getUserAchievements(count)
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