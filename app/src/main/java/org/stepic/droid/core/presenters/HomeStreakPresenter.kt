package org.stepic.droid.core.presenters

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import org.stepic.droid.core.presenters.contracts.HomeStreakView
import org.stepic.droid.di.home.HomeScope
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.RxOptional
import org.stepic.droid.util.StepikUtil
import org.stepic.droid.util.unwrapOptional
import org.stepik.android.data.user_activity.source.UserActivityRemoteDataSource
import javax.inject.Inject

@HomeScope
class HomeStreakPresenter
@Inject
constructor(
    @BackgroundScheduler
    private val backgroundScheduler: Scheduler,
    @MainScheduler
    private val mainScheduler: Scheduler,
    private val userActivityRemoteDataSource: UserActivityRemoteDataSource,
    private val sharedPreferences: SharedPreferenceHelper
): PresenterBase<HomeStreakView>() {
    private val compositeDisposable = CompositeDisposable()

    fun onNeedShowStreak() {
        compositeDisposable.add(Observable
                .fromCallable { RxOptional(sharedPreferences.profile?.id) }
                .unwrapOptional()
                .flatMapSingle { userActivityRemoteDataSource.getUserActivitiesRx(it) }
                .map { RxOptional(it.firstOrNull()?.pins) }
                .map { optional ->
                    optional.map { StepikUtil.getCurrentStreak(it) }
                }
                .unwrapOptional()
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribe({
                    showStreak(it)
                }, {
                    it.printStackTrace()
                }))
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