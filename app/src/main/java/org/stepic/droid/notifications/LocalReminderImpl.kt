package org.stepic.droid.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.MainThread
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.services.NewUserAlarmService
import org.stepic.droid.services.StreakAlarmService
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.store.operations.Table
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class LocalReminderImpl
@Inject constructor(
        private val threadPoolExecutor: ThreadPoolExecutor,
        private val sharedPreferenceHelper: SharedPreferenceHelper,
        private val databaseFacade: DatabaseFacade,
        private val context: Context,
        private val alarmManager: AlarmManager,
        private val analytic: Analytic) : LocalReminder {

    val isHandling = AtomicBoolean(false)

    override fun remindAboutApp(millis: Long?) {
        threadPoolExecutor.execute {
            val isNotLoading = isHandling.compareAndSet(/* expect */ false, true)
            if (isNotLoading) {
                try {
                    val isFirstDayNotificationShown = sharedPreferenceHelper.isNotificationWasShown(SharedPreferenceHelper.NotificationDay.DAY_ONE)
                    val isSevenDayNotificationShown = sharedPreferenceHelper.isNotificationWasShown(SharedPreferenceHelper.NotificationDay.DAY_SEVEN)
                    if (isFirstDayNotificationShown
                            && isSevenDayNotificationShown) {
                        //already shown.
                        //do not show again
                        return@execute
                    }
                    if (sharedPreferenceHelper.authResponseFromStore == null
                            || sharedPreferenceHelper.isStreakNotificationEnabled
                            || databaseFacade.getAllCourses(Table.enrolled).isNotEmpty()
                            || sharedPreferenceHelper.anyStepIsSolved()) {
                        return@execute
                    }


                    //now we can plan alarm

                    val now = DateTime.now(DateTimeZone.getDefault())
                    val scheduleMillis: Long
                    if (millis != null && millis > 0L && DateTime(millis).isAfterNow) {
                        scheduleMillis = millis // after reboot we already scheduled.
                    } else {
                        val dayDiff: Int =
                                if (!isFirstDayNotificationShown) {
                                    1
                                } else if (!isSevenDayNotificationShown) {
                                    7
                                } else {
                                    return@execute
                                }

                        val nowHour = now.hourOfDay().get()
                        val scheduleTime: DateTime
                        if (nowHour < 12) {
                            scheduleTime = now.plusDays(dayDiff).withHourOfDay(12)
                        } else if (nowHour >= 19) {
                            scheduleTime = now.plusDays(dayDiff + 1).withHourOfDay(12)
                        } else {
                            scheduleTime = now.plusDays(dayDiff)
                        }
                        scheduleMillis = scheduleTime.millis
                    }

                    sharedPreferenceHelper.saveNewUserRemindTimestamp(scheduleMillis)
                    // Sets an alarm - note this alarm will be lost if the phone is turned off and on again
                    val intent = Intent(context, NewUserAlarmService::class.java)
                    intent.putExtra(NewUserAlarmService.notificationTimestampSentKey, scheduleMillis)
                    val pendingIntent = PendingIntent.getService(context, NewUserAlarmService.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    alarmManager.cancel(pendingIntent)//timer should not be triggered

                    scheduleCompat(scheduleMillis, AlarmManager.INTERVAL_HALF_HOUR, pendingIntent)

                    val dayType = if (!isFirstDayNotificationShown) {
                        SharedPreferenceHelper.NotificationDay.DAY_ONE
                    } else if (!isSevenDayNotificationShown) {
                        SharedPreferenceHelper.NotificationDay.DAY_SEVEN
                    } else {
                        null
                    }
                    analytic.reportEvent(Analytic.Notification.REMIND_SCHEDULED, dayType?.name ?: "")
                } finally {
                    isHandling.set(false)
                }
            }
        }
    }

    val stateNotificationHandling = AtomicBoolean(false)

    override fun userChangeStateOfNotification() {
        threadPoolExecutor.execute {
            val isNotLoading = stateNotificationHandling.compareAndSet(/* expect */ false, true)
            if (isNotLoading) {
                try {
                    cancelPreviousStreakNotification()
                    if (sharedPreferenceHelper.isStreakNotificationEnabled) {
                        //plan new alarm
                        val hour = sharedPreferenceHelper.timeNotificationCode
                        val now = DateTime.now()

                        //start of interval
                        var nextNotification = now
                                .withHourOfDay(hour)
                                .withMinuteOfHour(0)
                                .withSecondOfMinute(0)
                                .withMillisOfSecond(0)
                        if (nextNotification.isBefore(now)) {
                            nextNotification = nextNotification.plusDays(1)
                        }

                        val intent = Intent(context, StreakAlarmService::class.java)
                        val pendingIntent = PendingIntent.getService(context, StreakAlarmService.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)


                        scheduleCompat(nextNotification.millis, AlarmManager.INTERVAL_HOUR, pendingIntent)
                    }
                } finally {
                    stateNotificationHandling.set(false)
                }
            }
        }
    }

    private fun cancelPreviousStreakNotification() {
        val intent = Intent(context, StreakAlarmService::class.java)
        val pendingIntent: PendingIntent? = PendingIntent.getService(context, StreakAlarmService.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        pendingIntent?.let {
            pendingIntent.cancel()
            alarmManager.cancel(pendingIntent)//timer should not be triggered
        }
    }

    @MainThread
    override fun remindAboutApp() {
        remindAboutApp(null)
    }

    private fun scheduleCompat(scheduleMillis: Long, interval: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT < 23) {
            if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setWindow(AlarmManager.RTC_WAKEUP, scheduleMillis, interval, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, scheduleMillis + interval / 2, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduleMillis + interval / 2, pendingIntent)
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, DateTime.now().millis + 15000, pendingIntent) //DEBUG PURPOSE ONLY
        }
    }
}
