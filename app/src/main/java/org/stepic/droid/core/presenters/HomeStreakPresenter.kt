package org.stepic.droid.core.presenters

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.core.presenters.contracts.HomeStreakView
import org.stepic.droid.di.home.HomeScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.RxOptional
import org.stepic.droid.util.StepikUtil
import org.stepic.droid.util.emptyOnErrorStub
import org.stepic.droid.util.toMaybe
import org.stepic.droid.util.unwrapOptional
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import javax.inject.Inject

@HomeScope
class HomeStreakPresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val userActivityRepository: UserActivityRepository,
    private val sharedPreferences: SharedPreferenceHelper
): PresenterBase<HomeStreakView>() {
    private val compositeDisposable = CompositeDisposable()

    fun onNeedShowStreak() {
        compositeDisposable += Maybe
            .fromCallable { sharedPreferences.profile?.id }
            .flatMapSingleElement(userActivityRepository::getUserActivities)
            .flatMap { userActivities ->
                userActivities
                    .firstOrNull()
                    ?.pins
                    ?.let(StepikUtil::getCurrentStreak)
                    .toMaybe()
            }
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = ::showStreak,
                onError = emptyOnErrorStub
            )
    }

    private fun showStreak(streak: Int) {
        if (streak > 0) {
            view?.showStreak(streak)
        } else {
            view?.onEmptyStreak()
        }
    }

    override fun detachView(view: HomeStreakView) {
        compositeDisposable.clear()
        super.detachView(view)
    }
}