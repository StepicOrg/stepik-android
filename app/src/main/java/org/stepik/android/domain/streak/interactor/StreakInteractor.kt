package org.stepik.android.domain.streak.interactor

import io.reactivex.Maybe
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.StepikUtil
import org.stepic.droid.util.toMaybe
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

    fun onNeedShowStreak(): Maybe<Int> =
        Maybe
            .fromCallable { sharedPreferenceHelper.profile?.id }
            .flatMapSingleElement { userActivityRepository.getUserActivities(it) }
            .flatMap { it.firstOrNull()?.pins.toMaybe() }
            .map { StepikUtil.getCurrentStreak(it) }

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