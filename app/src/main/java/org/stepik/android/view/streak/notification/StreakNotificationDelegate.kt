package org.stepik.android.view.streak.notification

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.TaskStackBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.StepikUtil
import org.stepik.android.domain.streak.analytic.StreakNotificationClicked
import org.stepik.android.domain.streak.analytic.StreakNotificationDismissed
import org.stepik.android.domain.streak.analytic.StreakNotificationShown
import org.stepik.android.domain.user_activity.repository.UserActivityRepository
import org.stepik.android.view.base.receiver.DismissedNotificationReceiver
import org.stepik.android.view.notification.NotificationDelegate
import org.stepik.android.view.notification.StepikNotificationManager
import org.stepik.android.view.notification.helpers.NotificationHelper
import org.stepik.android.view.profile.ui.activity.ProfileActivity
import org.stepik.android.view.streak.model.StreakNotificationType
import java.util.Calendar
import javax.inject.Inject

class StreakNotificationDelegate
@Inject
constructor(
    private val context: Context,
    private val analytic: Analytic,
    private val userActivityRepository: UserActivityRepository,
    private val screenManager: ScreenManager,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val notificationHelper: NotificationHelper,
    stepikNotificationManager: StepikNotificationManager
) : NotificationDelegate("show_streak_notification", stepikNotificationManager) {
    companion object {
        const val STREAK_NOTIFICATION_CLICKED = "streak_notification_clicked"
        private const val STREAK_NOTIFICATION_ID = 3214L
    }

    override fun onNeedShowNotification() {
        if (sharedPreferenceHelper.isStreakNotificationEnabled) {
            scheduleStreakNotification()
            val numberOfStreakNotifications = sharedPreferenceHelper.numberOfStreakNotifications
            if (numberOfStreakNotifications < AppConstants.MAX_NUMBER_OF_NOTIFICATION_STREAK) {
                try {
                    val pins: ArrayList<Long> = userActivityRepository.getUserActivities(sharedPreferenceHelper.profile?.id ?: throw Exception("User is not auth"))
                        .blockingGet()
                        .firstOrNull()
                        ?.pins!!
                    val (currentStreak, isSolvedToday) = StepikUtil.getCurrentStreakExtended(pins)
                    if (currentStreak <= 0) {
                        analytic.reportEvent(Analytic.Streak.GET_ZERO_STREAK_NOTIFICATION)
                        showNotificationWithoutStreakInfo(StreakNotificationType.ZERO)
                    } else {
                        // if current streak is > 0 -> streaks works! -> continue send it
                        // it will reset before sending, after sending it will be incremented
                        sharedPreferenceHelper.resetNumberOfStreakNotifications()
                        if (isSolvedToday) {
                            showNotificationStreakImprovement(currentStreak)
                        } else {
                            showNotificationWithStreakCallToAction(currentStreak)
                        }
                    }
                } catch (exception: Exception) {
                    // no internet || cant get streaks -> show some notification without streak information.
                    analytic.reportEvent(Analytic.Streak.GET_NO_INTERNET_NOTIFICATION)
                    showNotificationWithoutStreakInfo(StreakNotificationType.NO_INTERNET)
                    return
                } finally {
                    sharedPreferenceHelper.incrementNumberOfNotifications()
                }
            } else {
                // too many ignored notifications about streaks
                streakNotificationNumberIsOverflow()
            }
        }
    }

    fun scheduleStreakNotification() {
        if (sharedPreferenceHelper.isStreakNotificationEnabled) {
            // plan new alarm
            val hour = sharedPreferenceHelper.timeNotificationCode
            val now = DateTimeHelper.nowUtc()
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            var nextNotificationMillis = calendar.timeInMillis

            if (nextNotificationMillis < now) {
                nextNotificationMillis += AppConstants.MILLIS_IN_24HOURS
            }
            scheduleNotificationAt(nextNotificationMillis)
        }
    }

    private fun showNotificationStreakImprovement(currentStreak: Int) {
        val message = context.resources.getString(R.string.streak_notification_message_improvement, currentStreak)
        showNotificationStreakBase(message, StreakNotificationType.SOLVED_TODAY)
    }

    private fun showNotificationWithStreakCallToAction(currentStreak: Int) {
        val message = context.resources.getQuantityString(R.plurals.streak_notification_message_call_to_action, currentStreak, currentStreak)
        showNotificationStreakBase(message, StreakNotificationType.NOT_SOLVED_TODAY)
    }

    private fun showNotificationWithoutStreakInfo(notificationType: StreakNotificationType) {
        val message = context.resources.getString(R.string.streak_notification_empty_number)
        showNotificationStreakBase(message, notificationType)
    }

    private fun showNotificationStreakBase(message: String, notificationType: StreakNotificationType) {
        val taskBuilder: TaskStackBuilder = getStreakNotificationTaskBuilder(notificationType)
        val notification = notificationHelper.makeSimpleNotificationBuilder(stepikNotification = null,
                justText = message,
                taskBuilder = taskBuilder,
                title = context.getString(R.string.time_to_learn_notification_title),
                deleteIntent = getDeleteIntentForStreaks(notificationType), id = STREAK_NOTIFICATION_ID
        )
        analytic.report(StreakNotificationShown(notificationType.type))
        showNotification(STREAK_NOTIFICATION_ID, notification.build())
    }

    private fun getStreakNotificationTaskBuilder(notificationType: StreakNotificationType): TaskStackBuilder {
        val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        val myCoursesIntent = screenManager.getMyCoursesIntent(context) // This opens MainFeedActivity
        myCoursesIntent.action = STREAK_NOTIFICATION_CLICKED
        myCoursesIntent.putExtra(MainFeedActivity.EXTRA_PARCELABLE_ANALYTIC_EVENT, StreakNotificationClicked(notificationType.type).toBundle())
        taskBuilder.addNextIntent(myCoursesIntent)
        return taskBuilder
    }

    private fun streakNotificationNumberIsOverflow() {
        sharedPreferenceHelper.isStreakNotificationEnabled = false
        val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        val profileIntent = screenManager.getProfileIntent(context)
        taskBuilder.addParentStack(ProfileActivity::class.java)
        taskBuilder.addNextIntent(profileIntent)
        val message = context.getString(R.string.streak_notification_not_working)
        val notification = notificationHelper.makeSimpleNotificationBuilder(stepikNotification = null,
                justText = message,
                taskBuilder = taskBuilder,
                title = context.getString(R.string.time_to_learn_notification_title), id = STREAK_NOTIFICATION_ID
        )
        showNotification(STREAK_NOTIFICATION_ID, notification.build())
    }

    private fun getDeleteIntentForStreaks(notificationType: StreakNotificationType): PendingIntent {
        val deleteIntent = DismissedNotificationReceiver.createIntent(context, StreakNotificationDismissed(notificationType.type).toBundle())
        return PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)
    }
}