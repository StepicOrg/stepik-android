package org.stepik.android.domain.streak.interactor

import io.reactivex.Maybe
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.view.streak.notification.StreakNotificationScheduler
import javax.inject.Inject

class StreakInteractor
@Inject
constructor(
    private val userActivityRepository: UserActivityRepository,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val streakNotificationScheduler: StreakNotificationScheduler
) {

    fun needShowStreakDialog(): Boolean =
        isStreakNotificationEnabled() &&
        canShowStreakDialog() &&
        isAuthResponseFromStore()

    fun onNeedShowStreak(): Maybe<Int> =
        Maybe
            .fromCallable { sharedPreferenceHelper.profile?.id }
            .flatMapSingleElement { userActivityRepository.getUserActivitySummary(it) }
            .map { it.recentStrike }

    fun setStreakTime(timeIntervalCode: Int) {
        sharedPreferenceHelper.isStreakNotificationEnabled = true
        sharedPreferenceHelper.timeNotificationCode = timeIntervalCode
        streakNotificationScheduler.scheduleStreakNotification()
    }

    fun wasStreakDialogSeenOnHomeScreen(): Boolean =
        sharedPreferenceHelper.wasStreakDialogSeenHomeScreen

    fun onStreakDialogSeenOnHomeScreen() {
        sharedPreferenceHelper.putWasStreakDialogSeenHomeScreen(true)
    }

    private fun isStreakNotificationEnabled(): Boolean =
        sharedPreferenceHelper.isStreakNotificationEnabledNullable == null // default value, user not change in profile

    private fun canShowStreakDialog(): Boolean =
        sharedPreferenceHelper.canShowStreakDialog()

    private fun isAuthResponseFromStore(): Boolean =
        sharedPreferenceHelper.authResponseFromStore != null
}
