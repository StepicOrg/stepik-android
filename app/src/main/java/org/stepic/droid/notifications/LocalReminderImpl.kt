package org.stepic.droid.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.MainThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.services.NewUserAlarmService
import org.stepic.droid.services.StreakAlarmService
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import java.util.*
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

    private val isHandling = AtomicBoolean(false)

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

                    val now = DateTimeHelper.nowLocal()
                    val scheduleMillis: Long
                    if (millis != null && millis > 0L && millis > now) {
                        scheduleMillis = millis // after reboot we already scheduled.
                    } else {
                        val dayDiff: Int =
                                when {
                                    !isFirstDayNotificationShown -> 1
                                    !isSevenDayNotificationShown -> 7
                                    else -> return@execute
                                }


                        val calendar = Calendar.getInstance()
                        val nowHour = calendar.get(Calendar.HOUR_OF_DAY)
                        calendar.set(Calendar.HOUR_OF_DAY, 12)
                        val nowAt12 = DateTimeHelper.calendarToLocalMillis(calendar)
                        scheduleMillis = when {
                            nowHour < 12 -> nowAt12 + AppConstants.MILLIS_IN_24HOURS * dayDiff
                            nowHour >= 19 -> nowAt12 + AppConstants.MILLIS_IN_24HOURS * (dayDiff + 1)
                            else -> now + AppConstants.MILLIS_IN_24HOURS * dayDiff
                        }
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

    private val stateNotificationHandling = AtomicBoolean(false)

    override fun userChangeStateOfNotification() {
        threadPoolExecutor.execute {
            val isNotLoading = stateNotificationHandling.compareAndSet(/* expect */ false, true)
            if (isNotLoading) {
                try {
                    cancelPreviousStreakNotification()
                    if (sharedPreferenceHelper.isStreakNotificationEnabled) {
                        //plan new alarm
                        val hour = sharedPreferenceHelper.timeNotificationCode
                        val now = DateTimeHelper.nowLocal()
                        val calendar = Calendar.getInstance(TimeZone.getDefault())
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        var nextNotificationMillis = DateTimeHelper.calendarToLocalMillis(calendar)

                        if (nextNotificationMillis < now) {
                            nextNotificationMillis += AppConstants.MILLIS_IN_24HOURS
                        }

                        val intent = Intent(context, StreakAlarmService::class.java)
                        val pendingIntent = PendingIntent.getService(context, StreakAlarmService.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)


                        scheduleCompat(nextNotificationMillis, AlarmManager.INTERVAL_HOUR, pendingIntent)
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
