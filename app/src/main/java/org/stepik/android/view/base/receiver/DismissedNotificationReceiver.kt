package org.stepik.android.view.base.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepik.android.domain.personal_deadlines.analytic.DeadlinesNotificationDismissed
import org.stepik.android.domain.remind.analytic.RemindAppNotificationDismissed
import org.stepik.android.domain.remind.analytic.RemindRegistrationNotificationDismissed
import org.stepik.android.domain.retention.analytic.RetentionNotificationDismissed
import org.stepik.android.domain.streak.analytic.StreakNotificationDismissed
import org.stepik.android.view.personal_deadlines.model.DeadlinesNotificationData
import org.stepik.android.view.retention.model.RetentionNotificationData
import javax.inject.Inject

class DismissedNotificationReceiver : BroadcastReceiver() {
    companion object {
        const val REMIND_APP_NOTIFICATION_DISMISSED = "remind_app_notification_dismissed"
        const val REMIND_REGISTRATION_NOTIFICATION_DISMISSED = "remind_registration_notification_dismissed"
        const val RETENTION_NOTIFICATION_DISMISSED = "retention_notification_dismissed"
        const val DEADLINES_NOTIFICATION_DISMISSED = "deadlines_notification_dismissed"
        const val STREAK_NOTIFICATION_DISMISSED = "streak_notification_dismissed"

        const val REQUEST_CODE = 13202

        private const val EXTRA_RETENTION_DATA = "retention_data"
        private const val EXTRA_DEADLINES_DATA = "deadlines_data"
        private const val EXTRA_STREAK_TYPE = "streak_type"

        fun createIntent(context: Context, action: String): Intent =
            Intent(context, DismissedNotificationReceiver::class.java)
                .setAction(action)

        fun createIntent(context: Context, action: String, retentionNotificationData: RetentionNotificationData): Intent =
            Intent(context, DismissedNotificationReceiver::class.java)
                .setAction(action)
                .putExtra(EXTRA_RETENTION_DATA, retentionNotificationData)

        fun createIntent(context: Context, action: String, deadlinesNotificationData: DeadlinesNotificationData): Intent =
            Intent(context, DismissedNotificationReceiver::class.java)
                .setAction(action)
                .putExtra(EXTRA_DEADLINES_DATA, deadlinesNotificationData)

        fun createIntent(context: Context, action: String, streakType: String): Intent =
            Intent(context, DismissedNotificationReceiver::class.java)
                .setAction(action)
                .putExtra(EXTRA_STREAK_TYPE, streakType)
    }

    @Inject
    lateinit var analytic: Analytic

    init {
        App.component().inject(this)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        when (action) {
            REMIND_APP_NOTIFICATION_DISMISSED ->
                analytic.report(RemindAppNotificationDismissed)

            REMIND_REGISTRATION_NOTIFICATION_DISMISSED ->
                analytic.report(RemindRegistrationNotificationDismissed)

            RETENTION_NOTIFICATION_DISMISSED -> {
                val retentionNotificationData = intent
                    .getParcelableExtra<RetentionNotificationData>(EXTRA_RETENTION_DATA)
                    .takeIf { it != null }
                    ?: return

                analytic.report(RetentionNotificationDismissed(retentionNotificationData.retentionDay))
            }

            DEADLINES_NOTIFICATION_DISMISSED -> {
                val deadlinesNotificationData = intent
                    .getParcelableExtra<DeadlinesNotificationData>(EXTRA_DEADLINES_DATA)
                    .takeIf { it != null }
                    ?: return

                analytic.report(DeadlinesNotificationDismissed(deadlinesNotificationData.course, deadlinesNotificationData.hours))
            }

            STREAK_NOTIFICATION_DISMISSED -> {
                val streakType = intent.getStringExtra(EXTRA_STREAK_TYPE) ?: return
                analytic.report(StreakNotificationDismissed(streakType))
            }
        }
    }
}