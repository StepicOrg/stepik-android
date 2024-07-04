package org.stepik.android.view.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.isNotificationPermissionGranted
import org.stepik.android.view.notification.extension.PendingIntentCompat
import org.stepik.android.view.notification.receiver.AlarmReceiver
import javax.inject.Inject

class StepikNotificationManagerImpl
@Inject
constructor(
    private val context: Context,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : StepikNotificationManager {

    private val alarmManager: AlarmManager by lazy { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun scheduleNotification(id: String, millis: Long) {
        val intent = AlarmReceiver
                .createIntent(context, id)

        val pendingIntent = PendingIntentCompat
                .getBroadcast(context, AlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.cancel(pendingIntent)
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, millis, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent)
        // TODO APPS-3670: re-check Notifications

        sharedPreferenceHelper.putAlarmTimestamp(id, millis)
    }

    override fun rescheduleActiveNotification(id: String) {
        val millis = sharedPreferenceHelper.getAlarmTimestamp(id)
        if (millis > 0L && millis > DateTimeHelper.nowUtc()) {
            scheduleNotification(id, millis)
        }
    }

    override fun showNotification(id: Long, notification: Notification) {
        if (!context.isNotificationPermissionGranted()) {
            return
        }
        notificationManager.notify(id.toInt(), notification)
    }
}