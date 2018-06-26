package org.stepic.droid.features.achievements.presenters

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.core.presenters.PresenterBase
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.features.achievements.repository.AchievementsRepository
import org.stepic.droid.util.addDisposable
import javax.inject.Inject
import kotlin.properties.Delegates

class AchievementsPresenter
@Inject
constructor(
        private val achievementsRepository: AchievementsRepository,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
): PresenterBase<AchievementsView>() {
    private val compositeDisposable = CompositeDisposable()

    private var state by Delegates.observable(AchievementsView.State.Idle as AchievementsView.State) { _, _, newState -> setViewState(newState) }

    fun showAchievementsForUser(userId: Long, count: Int = -1, forceUpdate: Boolean = false) {
        if (state == AchievementsView.State.Idle || (forceUpdate && state == AchievementsView.State.Error)) {
            state = AchievementsView.State.Loading
            compositeDisposable addDisposable achievementsRepository.getAchievements(userId, count)
                    .subscribeOn(backgroundScheduler)
                    .observeOn(mainScheduler)
                    .subscribeBy({
                        state = AchievementsView.State.Error
                    }) {
                        state = AchievementsView.State.AchievementsLoaded(it)
                    }
        } else {
            setViewState(state)
        }
    }

    private fun setViewState(newState: AchievementsView.State) {
        when(newState) {
            is AchievementsView.State.AchievementsLoaded ->
                view?.showAchievements(newState.achievements)

            is AchievementsView.State.Loading ->
                view?.onAchievementsLoading()

            is AchievementsView.State.Error ->
                view?.onAchievementsLoadingError()
        }
    }
}