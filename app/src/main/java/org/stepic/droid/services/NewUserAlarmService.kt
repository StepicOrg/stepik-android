package org.stepic.droid.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import org.stepic.droid.base.App
import org.stepic.droid.features.deadlines.notifications.DeadlinesNotificationsManager
import org.stepic.droid.notifications.StepikNotificationManager
import javax.inject.Inject

class NewUserAlarmService : IntentService("NewUserAlarm") {
    companion object {
        const val NOTIFICATION_TIMESTAMP_SENT_KEY = "notificationTimestampKey"
        const val REQUEST_CODE = 177

        const val SHOW_REGISTRATION_NOTIFICATION = "show_registration_notification"
        const val SHOW_NEW_USER_NOTIFICATION = "show_new_user_notification"
    }

    @Inject
    lateinit var stepikNotificationManager: StepikNotificationManager

    @Inject
    lateinit var deadlinesNotificationsManager: DeadlinesNotificationsManager

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        App.component().inject(this)
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            SHOW_REGISTRATION_NOTIFICATION ->
                stepikNotificationManager.showRegistrationRemind()
            DeadlinesNotificationsManager.SHOW_DEADLINES_NOTIFICATION ->
                deadlinesNotificationsManager.showDeadlinesNotifications()
            else ->
                stepikNotificationManager.showLocalNotificationRemind()
        }
    }
}
