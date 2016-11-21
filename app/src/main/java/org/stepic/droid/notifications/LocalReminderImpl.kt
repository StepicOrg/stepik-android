package org.stepic.droid.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.annotation.MainThread
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.services.NewUserAlarmService
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.store.operations.Table
import timber.log.Timber
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.atomic.AtomicBoolean

class LocalReminderImpl(val threadPoolExecutor: ThreadPoolExecutor,
                        val sharedPreferenceHelper: SharedPreferenceHelper,
                        val databaseFacade: DatabaseFacade,
                        val context: Context,
                        val alarmManager: AlarmManager,
                        val analytic: Analytic) : LocalReminder {

    val isHandling = AtomicBoolean(false)

    @MainThread
    override fun remindAboutApp() {
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
                    if (sharedPreferenceHelper.authResponseFromStore == null) {
                        return@execute
                    }

                    if (databaseFacade.getAllCourses(Table.enrolled).isNotEmpty()) {
                        return@execute
                    }

                    if (sharedPreferenceHelper.anyStepIsSolved()) {
                        return@execute
                    }

                    //now we can plan alarm

                    val dayDiff: Int =
                            if (!isFirstDayNotificationShown) {
                                1
                            } else if (!isSevenDayNotificationShown) {
                                7
                            } else {
                                return@execute
                            }

                    val now = DateTime.now(DateTimeZone.getDefault())

                    val nowHour = now.hourOfDay().get()
                    Timber.d("hour of day = $nowHour")
                    val scheduleTime: DateTime
                    if (nowHour < 12) {
                        scheduleTime = now.plusDays(dayDiff).withHourOfDay(12)
                    } else if (nowHour >= 19) {
                        scheduleTime = now.plusDays(dayDiff + 1).withHourOfDay(12)
                    } else {
                        scheduleTime = now.plusDays(dayDiff)
                    }
//                    val scheduleMillis: Long = scheduleTime.millis// FIXME: uncomment it & and comment 2 below
                    val debugTime = now.plus(15 * 1000)
                    val scheduleMillis: Long = debugTime.millis
                    Timber.d("now: $now")
                    Timber.d("planned: $scheduleTime")
                    Timber.d("debug planned: $debugTime")

                    sharedPreferenceHelper.saveNewUserTimestamp(scheduleMillis)
                    // Sets an alarm - note this alarm will be lost if the phone is turned off and on again
                    val intent = Intent(context, NewUserAlarmService::class.java)
                    intent.putExtra(NewUserAlarmService.notificationTimestampSentKey, scheduleMillis)
                    val pendingIntent = PendingIntent.getService(context, NewUserAlarmService.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    alarmManager.cancel(pendingIntent)//timer should not be triggered
                    alarmManager.set(AlarmManager.RTC_WAKEUP, scheduleMillis, pendingIntent)


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
}
