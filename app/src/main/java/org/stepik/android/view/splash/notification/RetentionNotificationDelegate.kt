package org.stepik.android.view.splash.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.TaskStackBuilder
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.notifications.model.RetentionNotificationType
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.activities.SplashActivity
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.base.analytic.BUNDLEABLE_ANALYTIC_EVENT
import org.stepik.android.domain.base.analytic.toBundle
import org.stepik.android.domain.retention.analytic.RetentionNotificationClicked
import org.stepik.android.domain.retention.analytic.RetentionNotificationDismissed
import org.stepik.android.domain.retention.analytic.RetentionNotificationShown
import org.stepik.android.domain.user_courses.repository.UserCoursesRepository
import org.stepik.android.view.base.receiver.DismissedNotificationReceiver
import org.stepik.android.view.notification.NotificationDelegate
import org.stepik.android.view.notification.StepikNotificationManager
import org.stepik.android.view.notification.helpers.NotificationHelper
import java.util.Calendar
import javax.inject.Inject

class RetentionNotificationDelegate
@Inject
constructor(
    private val context: Context,
    private val userCoursesRepository: UserCoursesRepository,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val notificationHelper: NotificationHelper,
    private val analytic: Analytic,
    stepikNotificationManager: StepikNotificationManager
) : NotificationDelegate("show_retention_notification", stepikNotificationManager) {
    companion object {
        const val RETENTION_NOTIFICATION_CLICKED = "retention_notification_clicked"
        private const val RETENTION_NOTIFICATION_ID = 4432L
    }

    override fun onNeedShowNotification() {
        val lastSessionTimestamp = sharedPreferenceHelper.lastSessionTimestamp
        val now = DateTimeHelper.nowUtc()

        if (sharedPreferenceHelper.authResponseFromStore == null ||
            sharedPreferenceHelper.isStreakNotificationEnabled ||
            isEnrolledEmpty() ||
            now - lastSessionTimestamp < AppConstants.MILLIS_IN_24HOURS / 2
        ) {
            return
        }

        val notificationType =
            if (now - lastSessionTimestamp > AppConstants.MILLIS_IN_24HOURS * 2) {
                RetentionNotificationType.DAY3
            } else {
                RetentionNotificationType.DAY1
            }

        val deleteIntent = DismissedNotificationReceiver.createIntent(context, RetentionNotificationDismissed(notificationType.dayValue).toBundle())
        val deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val title = context.getString(notificationType.titleRes)
        val message = context.getString(notificationType.messageRes)

        val intent = Intent(context, SplashActivity::class.java)
        intent.putExtra(BUNDLEABLE_ANALYTIC_EVENT, RetentionNotificationClicked(notificationType.dayValue).toBundle())
        val taskBuilder = TaskStackBuilder
                .create(context)
                .addNextIntent(intent)

        val notification = notificationHelper.makeSimpleNotificationBuilder(
            stepikNotification = null,
            justText = message,
            taskBuilder = taskBuilder,
            title = title,
            deleteIntent = deletePendingIntent,
            id = RETENTION_NOTIFICATION_ID
        )

        analytic.report(RetentionNotificationShown(notificationType.dayValue))
        showNotification(RETENTION_NOTIFICATION_ID, notification.build())
        scheduleRetentionNotification()
    }

    fun scheduleRetentionNotification(shouldResetCounter: Boolean = true) {
        val now = DateTimeHelper.nowUtc()
        val oldTimestamp = sharedPreferenceHelper.retentionNotificationTimestamp

        val scheduleMillis: Long
        if (!shouldResetCounter && oldTimestamp > 0L && oldTimestamp > now) {
            scheduleMillis = oldTimestamp // after reboot we already scheduled.
        } else {
            val lastSessionTimestamp = sharedPreferenceHelper.lastSessionTimestamp
            val diff = now - lastSessionTimestamp

            val dayDiff: Int =
                if (shouldResetCounter ||
                    lastSessionTimestamp == 0L ||
                    diff <= AppConstants.MILLIS_IN_24HOURS) {
                    1
                } else {
                    3
                }

            val calendar = Calendar.getInstance()
            val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
            calendar.set(Calendar.HOUR_OF_DAY, 12)
            val nowAt12 = calendar.timeInMillis
            scheduleMillis = when {
                nowHour < 12 -> nowAt12 + AppConstants.MILLIS_IN_24HOURS * dayDiff
                nowHour >= 19 -> nowAt12 + AppConstants.MILLIS_IN_24HOURS * (dayDiff + 1)
                else -> now + AppConstants.MILLIS_IN_24HOURS * dayDiff
            }
        }
        scheduleNotificationAt(scheduleMillis)
        sharedPreferenceHelper.saveRetentionNotificationTimestamp(scheduleMillis)
    }

    private fun isEnrolledEmpty(): Boolean =
        userCoursesRepository
            .getUserCourses(sourceType = DataSourceType.CACHE)
            .blockingGet()
            .isEmpty()
}