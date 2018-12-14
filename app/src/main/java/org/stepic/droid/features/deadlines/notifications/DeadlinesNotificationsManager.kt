package org.stepic.droid.features.deadlines.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.subscribeBy
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepik.android.data.personal_deadlines.source.DeadlinesCacheDataSource
import org.stepic.droid.notifications.StepikNotificationManager
import org.stepic.droid.services.NewUserAlarmService
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.scheduleCompat
import javax.inject.Inject

@AppSingleton
class DeadlinesNotificationsManager
@Inject
constructor(
    private val context: Context,
    private val alarmManager: AlarmManager,
    private val deadlinesRecordOperations: DeadlinesCacheDataSource,
    private val stepikNotificationManager: StepikNotificationManager,

    @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
    @MainScheduler
        private val mainScheduler: Scheduler
) {
    companion object {
        const val SHOW_DEADLINES_NOTIFICATION = "show_deadlines_notification"

        private const val OFFSET_12HOURS = 12 * AppConstants.MILLIS_IN_1HOUR
        private const val OFFSET_36HOURS = 36 * AppConstants.MILLIS_IN_1HOUR
    }

    fun scheduleDeadlinesNotifications() {
        val now = DateTimeHelper.nowUtc()
        deadlinesRecordOperations.getClosestDeadlineTimestamp()
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                        onError = { scheduleDeadlinesNotificationAt(now,0) },
                        onSuccess = { scheduleDeadlinesNotificationAt(now, it) }
                )
    }

    private fun scheduleDeadlinesNotificationAt(now: Long, closestDeadline: Long) {
        val timestamp = when {
            closestDeadline - OFFSET_36HOURS > now ->
                closestDeadline - OFFSET_36HOURS
            closestDeadline - OFFSET_12HOURS > now ->
                closestDeadline - OFFSET_12HOURS
            else -> 0L
        }

        val intent = Intent(context, NewUserAlarmService::class.java)
        intent.action = SHOW_DEADLINES_NOTIFICATION
        intent.putExtra(NewUserAlarmService.NOTIFICATION_TIMESTAMP_SENT_KEY, timestamp)
        val pendingIntent = PendingIntent.getService(context, NewUserAlarmService.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.cancel(pendingIntent)

        if (timestamp > 0) {
            alarmManager.scheduleCompat(timestamp, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent)
        }
    }

    fun showDeadlinesNotifications() {
        val now = DateTimeHelper.nowUtc()
        deadlinesRecordOperations.getDeadlineRecordsForTimestamp(longArrayOf(now + OFFSET_12HOURS, now + OFFSET_36HOURS))
                .map { it.sortedBy { it.deadline }.distinctBy { it.courseId } }
                .subscribeOn(backgroundScheduler)
                .observeOn(backgroundScheduler)
                .doOnSuccess { deadlines ->
                    deadlines.forEach { stepikNotificationManager.showPersonalDeadlineNotification(it) }
                }
                .onErrorReturn { emptyList() }
                .subscribe { _, _ -> scheduleDeadlinesNotifications() }
    }
}