package org.stepik.android.presentation.achievement

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.achievement.interactor.AchievementInteractor
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class AchievementsPresenter
@Inject
constructor(
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

    fun showAchievementsForUser(userId: Long, isMyProfile: Boolean, forceUpdate: Boolean = false) {
        if (state == AchievementsView.State.Idle || (forceUpdate && state == AchievementsView.State.Error)) {
            state = AchievementsView.State.Loading
            compositeDisposable += achievementInteractor
                .getAchievements(userId)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { achievements ->
                        state = AchievementsView.State.AchievementsLoaded(achievements, isMyProfile)
                    },
                    onError = { state = AchievementsView.State.Error }
                )
        }
    }
}