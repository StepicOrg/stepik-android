package org.stepic.droid.features.deadlines.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.support.annotation.WorkerThread
import org.stepik.android.data.personal_deadlines.source.DeadlinesCacheDataSource
import org.stepic.droid.notifications.StepikNotificationManager
import org.stepic.droid.receivers.AlarmReceiver
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.scheduleCompat
import org.stepik.android.cache.personal_deadlines.model.DeadlineEntity
import javax.inject.Inject

class DeadlinesNotificationsManager
@Inject
constructor(
    private val context: Context,
    private val alarmManager: AlarmManager,
    private val deadlinesCacheDataSource: DeadlinesCacheDataSource,
    private val stepikNotificationManager: StepikNotificationManager
) {
    companion object {
        const val SHOW_DEADLINES_NOTIFICATION = "show_deadlines_notification"

        private const val OFFSET_12HOURS = 12 * AppConstants.MILLIS_IN_1HOUR
        private const val OFFSET_36HOURS = 36 * AppConstants.MILLIS_IN_1HOUR
    }

    @WorkerThread
    fun scheduleDeadlinesNotifications() {
        val now = DateTimeHelper.nowUtc()
        try {
            val timestamp = deadlinesCacheDataSource
                .getClosestDeadlineTimestamp()
                .blockingGet()
            scheduleDeadlinesNotificationAt(now, timestamp)
        } catch (_: Exception) {
            scheduleDeadlinesNotificationAt(now,0)
        }
    }

    private fun scheduleDeadlinesNotificationAt(now: Long, closestDeadline: Long) {
        val timestamp = when {
            closestDeadline - OFFSET_36HOURS > now ->
                closestDeadline - OFFSET_36HOURS
            closestDeadline - OFFSET_12HOURS > now ->
                closestDeadline - OFFSET_12HOURS
            else -> 0L
        }

        val intent = AlarmReceiver
            .createIntent(context, action = SHOW_DEADLINES_NOTIFICATION, timestamp = timestamp)

        val pendingIntent = PendingIntent
            .getBroadcast(context, AlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)

        if (timestamp > 0) {
            alarmManager.scheduleCompat(timestamp, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent)
        }
    }

    @WorkerThread
    fun showDeadlinesNotifications() {
        val now = DateTimeHelper.nowUtc()
        deadlinesCacheDataSource
            .getDeadlineRecordsForTimestamp(longArrayOf(now + OFFSET_12HOURS, now + OFFSET_36HOURS))
            .map { it.sortedBy(DeadlineEntity::deadline).distinctBy(DeadlineEntity::courseId) }
            .doOnSuccess { deadlines ->
                deadlines.forEach { stepikNotificationManager.showPersonalDeadlineNotification(it) }
            }
            .doFinally {
                scheduleDeadlinesNotifications()
            }
            .onErrorReturn { emptyList() }
            .ignoreElement()
            .blockingAwait()
    }
}