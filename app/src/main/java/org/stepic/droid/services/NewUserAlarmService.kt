package org.stepic.droid.services

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import org.stepic.droid.base.App
import org.stepic.droid.features.deadlines.notifications.DeadlinesNotificationsManager
import org.stepic.droid.notifications.StepikNotificationManager
import javax.inject.Inject

class NewUserAlarmService : JobIntentService() {
    companion object {
        const val NOTIFICATION_TIMESTAMP_SENT_KEY = "notificationTimestampKey"

        const val SHOW_REGISTRATION_NOTIFICATION = "show_registration_notification"
        const val SHOW_NEW_USER_NOTIFICATION = "show_new_user_notification"

        private const val JOB_ID = 2400

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, NewUserAlarmService::class.java, JOB_ID,
                Intent(intent.action).putExtras(intent))
        }
    }

    @Inject
    lateinit var stepikNotificationManager: StepikNotificationManager

    @Inject
    lateinit var deadlinesNotificationsManager: DeadlinesNotificationsManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        App.component().inject(this)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleWork(intent: Intent) {
        when (intent.action) {
            SHOW_REGISTRATION_NOTIFICATION ->
                stepikNotificationManager.showRegistrationRemind()
            DeadlinesNotificationsManager.SHOW_DEADLINES_NOTIFICATION ->
                deadlinesNotificationsManager.showDeadlinesNotifications()
            else ->
                stepikNotificationManager.showLocalNotificationRemind()
        }
    }
}
