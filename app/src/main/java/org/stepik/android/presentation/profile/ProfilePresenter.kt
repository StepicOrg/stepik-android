package org.stepik.android.presentation.profile

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
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
    private val profileObservable: Observable<Profile>,

    @MainScheduler
    private val mainScheduler: Scheduler,

    @BackgroundScheduler
    private val backgroundScheduler: Scheduler
) : PresenterBase<ProfileView>() {

    fun showAchievementsForUser(userId: Long, count: Int = -1) {
        view?.onAchievementsLoading()
        compositeDisposable += profileInteractor
            .fetchAchievementsForUser(userId, count)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onError = { view?.onAchievementsLoadingError() },
                onSuccess = { view?.showAchievements(it) }
            )
    }

    fun showNotificationSetting() {
        compositeDisposable += profileInteractor
            .tryShowNotificationSetting()
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onError = { it.printStackTrace() },
                onSuccess = { view?.showNotificationEnabledState(it.first, it.second) }
            )
    }

    fun switchNotificationStreak(isChecked: Boolean) {
        compositeDisposable += profileInteractor
            .switchNotificationStreak(isChecked)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onError = { it.printStackTrace() },
                onComplete = { view?.hideNotificationTime(!isChecked) }
            )

    }

    fun setStreakTime(timeIntervalCode: Int) {
        compositeDisposable += profileInteractor
            .setStreakTime(timeIntervalCode)
            .subscribeOn(backgroundScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onError = { it.printStackTrace() },
                onSuccess = { view?.setNewTimeInterval(it) }
            )
    }

    private fun showLocalProfile(profile: Profile) {

    }

    private fun subscribeForProfileUpdates(profileId: Long) {
        compositeDisposable += profileObservable
            .filter { profileId == 0L || it.id == profileId }
            .observeOn(backgroundScheduler)
            .subscribe(::showLocalProfile)
    }


}