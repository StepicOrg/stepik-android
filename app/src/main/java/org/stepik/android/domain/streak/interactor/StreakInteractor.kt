package org.stepik.android.domain.streak.interactor

import io.reactivex.Observable
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.RxOptional
import org.stepic.droid.util.StepikUtil
import org.stepic.droid.util.unwrapOptional
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.view.streak.notification.StreakNotificationDelegate
import javax.inject.Inject

class StreakInteractor
@Inject
constructor(
    private val userActivityRepository: UserActivityRepository,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val streakNotificationDelegate: StreakNotificationDelegate
) {

    fun needShowStreakDialog(): Boolean =
        isStreakNotificationEnabled() &&
        canShowStreakDialog() &&
        isAuthResponseFromStore()

    fun onNeedShowStreak(): Observable<Int> =
        Observable.fromCallable { RxOptional(sharedPreferenceHelper.profile?.id) }
            .unwrapOptional()
            .flatMap { userActivityRepository.getUserActivities(it).toObservable() }
            .map { RxOptional(it.firstOrNull()?.pins) }
            .map { optional ->
                optional.map { StepikUtil.getCurrentStreak(it) }
            }
            .unwrapOptional()

    fun setStreakTime(timeIntervalCode: Int) {
        sharedPreferenceHelper.isStreakNotificationEnabled = true
        sharedPreferenceHelper.timeNotificationCode = timeIntervalCode
        streakNotificationDelegate.scheduleStreakNotification()
    }

    private fun isStreakNotificationEnabled(): Boolean =
        sharedPreferenceHelper.isStreakNotificationEnabledNullable == null // default value, user not change in profile

    private fun canShowStreakDialog(): Boolean =
        sharedPreferenceHelper.canShowStreakDialog()

    private fun isAuthResponseFromStore(): Boolean =
        sharedPreferenceHelper.authResponseFromStore != null
}