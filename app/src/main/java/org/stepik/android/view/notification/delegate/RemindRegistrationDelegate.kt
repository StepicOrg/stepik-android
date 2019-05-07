package org.stepik.android.view.notification.delegate

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.notifications.NotificationBroadcastReceiver
import org.stepic.droid.notifications.NotificationTimeChecker
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.StepikNotificationChannel
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.ui.activities.SplashActivity
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.view.notification.NotificationDelegate
import org.stepik.android.view.notification.StepikNotifManager
import javax.inject.Inject

class RemindRegistrationDelegate
@Inject constructor(
        private val context: Context,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val notificationTimeChecker: NotificationTimeChecker,
        private val analytic: Analytic,
        private val userPreferences: UserPreferences,
        private val stepikNotifManager: StepikNotifManager
) : NotificationDelegate("show_registration_notification", stepikNotifManager) {

    companion object {
        private const val REGISTRATION_REMIND_NOTIFICATION_ID = 5L
    }

    override fun onNeedShowNotification() {
        if (sharedPreferenceHelper.isEverLogged) return

        val intent = Intent(context, SplashActivity::class.java)
        val taskBuilder = TaskStackBuilder
                .create(context)
                .addNextIntent(intent)

        val title = context.getString(R.string.stepik_free_courses_title)
        val remindMessage = context.getString(R.string.registration_remind_message)
        showSimpleNotification(
                stepikNotification = null,
                justText = remindMessage,
                taskBuilder = taskBuilder,
                title = title,
                id = REGISTRATION_REMIND_NOTIFICATION_ID)
    }

    private fun showSimpleNotification(stepikNotification: Notification?, justText: String, taskBuilder: TaskStackBuilder, title: String?, deleteIntent: PendingIntent = getDeleteIntent(), id: Long) {
        val pendingIntent = taskBuilder.getPendingIntent(id.toInt(), PendingIntent.FLAG_ONE_SHOT) //fixme if it will overlay courses id -> bug

        val colorArgb = ColorUtil.getColorArgb(R.color.stepic_brand_primary)
        val notification = NotificationCompat.Builder(context, stepikNotification?.type?.channel?.channelId ?: StepikNotificationChannel.user.channelId)
                .setSmallIcon(R.drawable.ic_notification_icon_1)
                .setContentTitle(title)
                .setContentText(justText)
                .setColor(colorArgb)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(deleteIntent)


        notification.setStyle(NotificationCompat.BigTextStyle()
                .bigText(justText))
                .setContentText(justText)


        //if notification is null (for example for streaks) -> show it always with sound and vibrate

        val isNight = notificationTimeChecker.isNight(DateTimeHelper.nowLocal())
        if (isNight) {
            analytic.reportEvent(Analytic.Notification.NIGHT_WITHOUT_SOUND_AND_VIBRATE)
        }

        if (stepikNotification == null || !isNight) {
            addVibrationIfNeed(notification)
            addSoundIfNeed(notification)
        }

        showNotification(id, notification.build())
    }

    private fun getDeleteIntent(courseId: Long = -1): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = AppConstants.NOTIFICATION_CANCELED
        val bundle = Bundle()
        if (courseId > 0) {
            bundle.putSerializable(AppConstants.COURSE_ID_KEY, courseId)
        }
        intent.putExtras(bundle)
        //add course id for bundle
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun addVibrationIfNeed(builder: NotificationCompat.Builder) {
        if (userPreferences.isVibrateNotificationEnabled) {
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        }
    }

    private fun addSoundIfNeed(builder: NotificationCompat.Builder) {
        if (userPreferences.isSoundNotificationEnabled) {
            val stepicSound = Uri.parse("android.resource://"
                    + context.packageName + "/" + R.raw.default_sound)
            builder.setSound(stepicSound)
        }
    }
}