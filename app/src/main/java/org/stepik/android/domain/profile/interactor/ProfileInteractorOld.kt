package org.stepik.android.domain.profile.interactor

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.model.AchievementFlatItem
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.util.TimeIntervalUtil
import org.stepic.droid.util.first
import org.stepic.droid.util.toMaybe
import org.stepik.android.domain.achievement.repository.AchievementRepository
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.model.user.Profile
import org.stepik.android.model.user.User
import org.stepik.android.view.streak.notification.StreakNotificationDelegate
import javax.inject.Inject

class ProfileInteractorOld
@Inject
constructor(
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val achievementRepository: AchievementRepository,
    private val userActivityRepository: UserActivityRepository,
    private val analytic: Analytic,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val streakNotificationDelegate: StreakNotificationDelegate
) {
    fun fetchProfile(): Single<Profile> =
        profileRepository.getProfile()

    fun fetchProfile(userId: Long): Single<User> =
        userRepository.getUsers(userId).first()

    fun fetchAchievementsForUser(userId: Long, count: Int = -1): Single<List<AchievementFlatItem>> =
        achievementRepository.getAchievements(userId, count)

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

    fun fetchPins(userId: Long): Maybe<List<Long>> =
        userActivityRepository
            .getUserActivities(userId)
            .flatMapMaybe { it.firstOrNull()?.pins.toMaybe() }
}