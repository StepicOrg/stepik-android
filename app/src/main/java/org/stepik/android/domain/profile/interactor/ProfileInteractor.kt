package org.stepik.android.domain.profile.interactor

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.features.achievements.repository.AchievementsRepository
import org.stepic.droid.model.AchievementFlatItem
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.util.TimeIntervalUtil
import org.stepik.android.view.streak.notification.StreakNotificationDelegate
import javax.inject.Inject

class ProfileInteractor
@Inject
constructor(
    private val achievementsRepository: AchievementsRepository,
    private val analytic: Analytic,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val streakNotificationDelegate: StreakNotificationDelegate
) {
    fun fetchAchievementsForUser(userId: Long, count: Int = -1): Single<List<AchievementFlatItem>> =
        achievementsRepository.getAchievements(userId, count)

    /**
     * Streak related
     */
    fun tryShowNotificationSetting(): Single<Pair<Boolean, String>> =
        Single.fromCallable {
            val isEnabled = sharedPreferenceHelper.isStreakNotificationEnabled
            val code = sharedPreferenceHelper.timeNotificationCode
            val timeNotificationString = TimeIntervalUtil.values[code]
            Pair(isEnabled, timeNotificationString)
        }

    fun switchNotificationStreak(isChecked: Boolean): Completable =
        Completable.fromAction {
            sharedPreferenceHelper.isStreakNotificationEnabled = isChecked
            analytic.reportEvent(Analytic.Streak.SWITCH_NOTIFICATION_IN_MENU, isChecked.toString())
            streakNotificationDelegate.scheduleStreakNotification()
        }

    fun setStreakTime(timeIntervalCode: Int): Single<String> =
        Single.fromCallable {
            sharedPreferenceHelper.isStreakNotificationEnabled = true
            sharedPreferenceHelper.timeNotificationCode = timeIntervalCode
            val timePresentationString = TimeIntervalUtil.values[timeIntervalCode]
            streakNotificationDelegate.scheduleStreakNotification()
            timePresentationString
        }
}