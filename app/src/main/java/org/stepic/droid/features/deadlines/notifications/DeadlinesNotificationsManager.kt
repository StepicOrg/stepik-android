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
import org.stepic.droid.features.deadlines.storage.operations.DeadlinesRecordOperations
import org.stepic.droid.notifications.StepikNotificationManager
import org.stepic.droid.services.NewUserAlarmService
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.scheduleCompat
import javax.inject.Inject

@AppSingleton
class DeadlinesNotificationsManager
@Inject
constructor(
        private val context: Context,
        private val alarmManager: AlarmManager,
        private val deadlinesRecordOperations: DeadlinesRecordOperations,
        private val stepikNotificationManager: StepikNotificationManager,

        @BackgroundScheduler
        private val backgroundScheduler: Scheduler,
        @MainScheduler
        private val mainScheduler: Scheduler
) {
    companion object {
        const val SHOW_DEADLINES_NOTIFICATION = "show_deadlines_notification"
    }

    fun scheduleDeadlinesNotifications() {
        deadlinesRecordOperations.getClosestDeadlineTimestamp()
                .subscribeOn(backgroundScheduler)
                .observeOn(mainScheduler)
                .subscribeBy(
                        onError = { scheduleDeadlinesNotificationAt(0) },
                        onSuccess = { scheduleDeadlinesNotificationAt(it) }
                )
    }

    private fun scheduleDeadlinesNotificationAt(timestamp: Long) {
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
        deadlinesRecordOperations.getDeadlineRecordsForTimestamp(DateTimeHelper.nowUtc())
                .observeOn(mainScheduler)
                .subscribeOn(backgroundScheduler)
                .subscribeBy(
                        onError = {},
                        onSuccess = { deadlines ->
                            deadlines.forEach { stepikNotificationManager.showPersonalDeadlineNotification(it) }
                            scheduleDeadlinesNotifications()
                        }
                )
    }
}