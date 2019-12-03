package org.stepik.android.presentation.achievement

import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.domain.achievement.repository.AchievementRepository
import org.stepik.android.presentation.base.PresenterBase
import javax.inject.Inject

class AchievementsPresenter
@Inject
constructor(
    private val achievementRepository: AchievementRepository,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler
): PresenterBase<AchievementsView>() {
    private var state: AchievementsView.State =
        AchievementsView.State.Idle
        set(value) {
            field = value
            view?.setState(value)
        }

    override fun attachView(view: AchievementsView) {
        super.attachView(view)
        view.setState(state)
    }

    fun showAchievementsForUser(userId: Long, count: Int = -1, forceUpdate: Boolean = false) {
        if (state == AchievementsView.State.Idle || (forceUpdate && state == AchievementsView.State.Error)) {
            state = AchievementsView.State.Loading
            compositeDisposable += achievementRepository
                .getAchievements(userId, count)
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                    onSuccess = { state = AchievementsView.State.AchievementsLoaded(it) },
                    onError = { state = AchievementsView.State.Error }
                )
        }
    }
}