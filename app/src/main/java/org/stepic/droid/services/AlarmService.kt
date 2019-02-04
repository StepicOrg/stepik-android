package org.stepic.droid.services

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import org.stepic.droid.base.App
import org.stepic.droid.features.deadlines.notifications.DeadlinesNotificationsManager
import org.stepic.droid.notifications.StepikNotificationManager
import javax.inject.Inject

class AlarmService : JobIntentService() {
    companion object {
        private const val JOB_ID = 2400

        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, AlarmService::class.java, JOB_ID,
                Intent(intent.action).putExtras(intent))
        }
    }

    @Inject
    lateinit var stepikNotificationManager: StepikNotificationManager

    @Inject
    lateinit var deadlinesNotificationsManager: DeadlinesNotificationsManager

    override fun onCreate() {
        super.onCreate()
        App.component().inject(this)
    }

    override fun onHandleWork(intent: Intent) {
        when (intent.action) {
            StepikNotificationManager.SHOW_REGISTRATION_NOTIFICATION ->
                stepikNotificationManager.showRegistrationRemind()

            StepikNotificationManager.SHOW_STREAK_NOTIFICATION ->
                stepikNotificationManager.showStreakRemind()

            StepikNotificationManager.SHOW_NEW_USER_NOTIFICATION ->
                stepikNotificationManager.showLocalNotificationRemind()

            DeadlinesNotificationsManager.SHOW_DEADLINES_NOTIFICATION ->
                deadlinesNotificationsManager.showDeadlinesNotifications()
        }
    }
}
