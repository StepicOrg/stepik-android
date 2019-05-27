package org.stepik.android.view.notification.delegate

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.TaskStackBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.notifications.NotificationBroadcastReceiver
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.activities.ProfileActivity
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.StepikUtil
import org.stepic.droid.web.Api
import org.stepik.android.view.notification.NotificationDelegate
import org.stepik.android.view.notification.StepikNotifManager
import org.stepik.android.view.notification.helpers.NotificationHelper
import java.util.*
import javax.inject.Inject

class StreakDelegate
@Inject constructor(
    private val context: Context,
    private val analytic: Analytic,
    private val api: Api,
    private val screenManager: ScreenManager,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val notificationHelper: NotificationHelper,
    stepikNotifManager: StepikNotifManager
) : NotificationDelegate("show_streak_notification", stepikNotifManager) {
    companion object {
        private const val STREAK_NOTIFICATION_ID = 3214L
    }

    override fun onNeedShowNotification() {
        if (sharedPreferenceHelper.isStreakNotificationEnabled) {
            scheduleStreakNotification()
            val numberOfStreakNotifications = sharedPreferenceHelper.numberOfStreakNotifications
            if (numberOfStreakNotifications < AppConstants.MAX_NUMBER_OF_NOTIFICATION_STREAK) {
                try {
                    val pins: ArrayList<Long> = api.getUserActivities(sharedPreferenceHelper.profile?.id ?: throw Exception("User is not auth"))
                            .execute()
                            ?.body()
                            ?.userActivities
                            ?.firstOrNull()
                            ?.pins!!
                    val (currentStreak, isSolvedToday) = StepikUtil.getCurrentStreakExtended(pins)
                    if (currentStreak <= 0) {
                        analytic.reportEvent(Analytic.Streak.GET_ZERO_STREAK_NOTIFICATION)
                        showNotificationWithoutStreakInfo(Analytic.Streak.NotificationType.zero)
                    } else {
                        //if current streak is > 0 -> streaks works! -> continue send it
                        //it will reset before sending, after sending it will be incremented
                        sharedPreferenceHelper.resetNumberOfStreakNotifications()

                        val bundle = Bundle()
                        if (isSolvedToday) {
                            showNotificationStreakImprovement(currentStreak)
                            bundle.putString(Analytic.Streak.NOTIFICATION_TYPE_PARAM, Analytic.Streak.NotificationType.solvedToday.name)
                        } else {
                            showNotificationWithStreakCallToAction(currentStreak)
                            bundle.putString(Analytic.Streak.NOTIFICATION_TYPE_PARAM, Analytic.Streak.NotificationType.notSolvedToday.name)
                        }
                        analytic.reportEvent(Analytic.Streak.GET_NON_ZERO_STREAK_NOTIFICATION, bundle)
                    }
                } catch (exception: Exception) {
                    // no internet || cant get streaks -> show some notification without streak information.
                    analytic.reportEvent(Analytic.Streak.GET_NO_INTERNET_NOTIFICATION)
                    showNotificationWithoutStreakInfo(Analytic.Streak.NotificationType.noInternet)
                    return
                } finally {
                    sharedPreferenceHelper.incrementNumberOfNotifications()
                }
            } else {
                //too many ignored notifications about streaks
                streakNotificationNumberIsOverflow()
            }
        }
    }

    fun scheduleStreakNotification() {
        if (sharedPreferenceHelper.isStreakNotificationEnabled) {
            //plan new alarm
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
        showNotificationStreakBase(message, Analytic.Streak.NotificationType.solvedToday)
    }

    private fun showNotificationWithStreakCallToAction(currentStreak: Int) {
        val message = context.resources.getQuantityString(R.plurals.streak_notification_message_call_to_action, currentStreak, currentStreak)
        showNotificationStreakBase(message, Analytic.Streak.NotificationType.notSolvedToday)
    }

    private fun showNotificationWithoutStreakInfo(notificationType: Analytic.Streak.NotificationType) {
        val message = context.resources.getString(R.string.streak_notification_empty_number)
        showNotificationStreakBase(message, notificationType)
    }

    private fun showNotificationStreakBase(message: String, notificationType: Analytic.Streak.NotificationType) {
        val taskBuilder: TaskStackBuilder = getStreakNotificationTaskBuilder(notificationType)
        val notification = notificationHelper.makeSimpleNotificationBuilder(stepikNotification = null,
                justText = message,
                taskBuilder = taskBuilder,
                title = context.getString(R.string.time_to_learn_notification_title),
                deleteIntent = getDeleteIntentForStreaks(), id = STREAK_NOTIFICATION_ID)
        showNotification(STREAK_NOTIFICATION_ID, notification.build())
    }

    private fun getStreakNotificationTaskBuilder(notificationType: Analytic.Streak.NotificationType): TaskStackBuilder {
        val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        val myCoursesIntent = screenManager.getMyCoursesIntent(context)
        myCoursesIntent.action = AppConstants.OPEN_NOTIFICATION_FROM_STREAK
        myCoursesIntent.putExtra(Analytic.Streak.NOTIFICATION_TYPE_PARAM, notificationType)
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
                title = context.getString(R.string.time_to_learn_notification_title), id = STREAK_NOTIFICATION_ID)
        showNotification(STREAK_NOTIFICATION_ID, notification.build())
    }

    private fun getDeleteIntentForStreaks(): PendingIntent {
        val deleteIntent = Intent(context, NotificationBroadcastReceiver::class.java)
        deleteIntent.action = AppConstants.NOTIFICATION_CANCELED_STREAK
        val deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        return deletePendingIntent
    }
}