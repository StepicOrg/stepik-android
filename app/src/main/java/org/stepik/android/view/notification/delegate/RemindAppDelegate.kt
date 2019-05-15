package org.stepik.android.view.notification.delegate

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.TaskStackBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.model.CourseListType
import org.stepic.droid.notifications.NotificationBroadcastReceiver
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepic.droid.util.AppConstants
import org.stepik.android.view.notification.NotificationDelegate
import org.stepik.android.view.notification.StepikNotifManager
import org.stepik.android.view.notification.helpers.NotificationHelper
import javax.inject.Inject

class RemindAppDelegate
@Inject constructor(
    val context: Context,
    val sharedPreferenceHelper: SharedPreferenceHelper,
    val databaseFacade: DatabaseFacade,
    val analytic: Analytic,
    val screenManager: ScreenManager,
    val notificationHelper: NotificationHelper,
    stepikNotifManager: StepikNotifManager
) : NotificationDelegate("show_new_user_notification", stepikNotifManager) {
    companion object {
        private const val NEW_USER_REMIND_NOTIFICATION_ID = 4L
    }

    override fun onNeedShowNotification() {
        if (sharedPreferenceHelper.authResponseFromStore == null ||
            databaseFacade.getAllCourses(CourseListType.ENROLLED).isNotEmpty() ||
            sharedPreferenceHelper.anyStepIsSolved() || sharedPreferenceHelper.isStreakNotificationEnabled) {
            analytic.reportEvent(Analytic.Notification.REMIND_HIDDEN)
            return
        }
        val dayType = if (!sharedPreferenceHelper.isNotificationWasShown(SharedPreferenceHelper.NotificationDay.DAY_ONE)) {
            SharedPreferenceHelper.NotificationDay.DAY_ONE
        } else if (!sharedPreferenceHelper.isNotificationWasShown(SharedPreferenceHelper.NotificationDay.DAY_SEVEN)) {
            SharedPreferenceHelper.NotificationDay.DAY_SEVEN
        } else {
            null
        }

        val deleteIntent = Intent(context, NotificationBroadcastReceiver::class.java)
        deleteIntent.action = AppConstants.NOTIFICATION_CANCELED_REMINDER
        val deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        //now we can show notification
        val intent = screenManager.getCatalogIntent(context)
        intent.action = AppConstants.OPEN_NOTIFICATION_FOR_ENROLL_REMINDER
        val analyticDayTypeName = dayType?.name ?: ""
        intent.putExtra(MainFeedActivity.reminderKey, analyticDayTypeName)
        val taskBuilder: TaskStackBuilder =
            TaskStackBuilder
                    .create(context)
                    .addNextIntent(intent)
        val title = context.resources.getString(R.string.stepik_free_courses_title)
        val remindMessage = context.resources.getString(R.string.local_remind_message)
        val notification = notificationHelper.makeSimpleNotificationBuilder(stepikNotification = null,
            justText = remindMessage,
            taskBuilder = taskBuilder,
            title = title,
            deleteIntent = deletePendingIntent,
            id = NEW_USER_REMIND_NOTIFICATION_ID
        )

        showNotification(NEW_USER_REMIND_NOTIFICATION_ID, notification.build())

        if (!sharedPreferenceHelper.isNotificationWasShown(SharedPreferenceHelper.NotificationDay.DAY_ONE)) {
            afterLocalNotificationShown(SharedPreferenceHelper.NotificationDay.DAY_ONE)
        } else if (!sharedPreferenceHelper.isNotificationWasShown(SharedPreferenceHelper.NotificationDay.DAY_SEVEN)) {
            afterLocalNotificationShown(SharedPreferenceHelper.NotificationDay.DAY_SEVEN)
        }

        // TODO Schedule remind about app
    }

    private fun afterLocalNotificationShown(day: SharedPreferenceHelper.NotificationDay) {
        analytic.reportEvent(Analytic.Notification.REMIND_SHOWN, day.name)
        sharedPreferenceHelper.setNotificationShown(day)
    }
}