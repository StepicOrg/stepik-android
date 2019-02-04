package org.stepic.droid.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.support.annotation.MainThread
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.model.CourseListType
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.receivers.AlarmReceiver
import org.stepic.droid.services.NewUserAlarmService
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.scheduleCompat
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
                            || databaseFacade.getAllCourses(CourseListType.ENROLLED).isNotEmpty()
                            || sharedPreferenceHelper.anyStepIsSolved()) {
                        return@execute
                    }


                    //now we can plan alarm

                    val now = DateTimeHelper.nowUtc()
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
                        val nowAt12 = calendar.timeInMillis
                        scheduleMillis = when {
                            nowHour < 12 -> nowAt12 + AppConstants.MILLIS_IN_24HOURS * dayDiff
                            nowHour >= 19 -> nowAt12 + AppConstants.MILLIS_IN_24HOURS * (dayDiff + 1)
                            else -> now + AppConstants.MILLIS_IN_24HOURS * dayDiff
                        }
                    }

                    sharedPreferenceHelper.saveNewUserRemindTimestamp(scheduleMillis)
                    // Sets an alarm - note this alarm will be lost if the phone is turned off and on again
                    val intent = AlarmReceiver
                        .createIntent(context, NewUserAlarmService.SHOW_NEW_USER_NOTIFICATION)

                    val pendingIntent = PendingIntent
                        .getBroadcast(context, AlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                    alarmManager.cancel(pendingIntent)//timer should not be triggered

                    alarmManager.scheduleCompat(scheduleMillis, AlarmManager.INTERVAL_HALF_HOUR, pendingIntent)

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
                        val now = DateTimeHelper.nowUtc()
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        var nextNotificationMillis = calendar.timeInMillis

                        if (nextNotificationMillis < now) {
                            nextNotificationMillis += AppConstants.MILLIS_IN_24HOURS
                        }

                        val intent = AlarmReceiver.createIntent(context, NewUserAlarmService.SHOW_STREAK_NOTIFICATION)
                        val pendingIntent = PendingIntent.getService(context, AlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)


                        alarmManager.scheduleCompat(nextNotificationMillis, AlarmManager.INTERVAL_HOUR, pendingIntent)
                    }
                } finally {
                    stateNotificationHandling.set(false)
                }
            }
        }
    }

    private fun cancelPreviousStreakNotification() {
        val intent = AlarmReceiver.createIntent(context, NewUserAlarmService.SHOW_STREAK_NOTIFICATION)
        val pendingIntent: PendingIntent? = PendingIntent.getService(context, AlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        pendingIntent?.let {
            pendingIntent.cancel()
            alarmManager.cancel(pendingIntent)//timer should not be triggered
        }
    }

    @MainThread
    override fun remindAboutApp() {
        remindAboutApp(null)
    }

    private val registrationRemindHandling = AtomicBoolean(false)

    override fun remindAboutRegistration() {
        threadPoolExecutor.execute {
            val isNotLoading = registrationRemindHandling.compareAndSet(false, true)
            if (!isNotLoading) return@execute
            try {
                if (sharedPreferenceHelper.authResponseFromStore != null) {
                    sharedPreferenceHelper.setHasEverLogged()
                }

                if (sharedPreferenceHelper.isEverLogged) return@execute

                val now = DateTimeHelper.nowUtc()
                val oldTimestamp = sharedPreferenceHelper.registrationRemindTimestamp

                val scheduleMillis = if (now < oldTimestamp) {
                    oldTimestamp
                } else {
                    if (oldTimestamp == 0L) { // means that notification wasn't shown before
                        now + AppConstants.MILLIS_IN_1HOUR
                    } else {
                        now + 2 * AppConstants.MILLIS_IN_1HOUR
                    }
                }

                val intent = AlarmReceiver
                    .createIntent(context, NewUserAlarmService.SHOW_REGISTRATION_NOTIFICATION)

                val pendingIntent = PendingIntent
                    .getBroadcast(context, AlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)

                alarmManager.cancel(pendingIntent)

                alarmManager.scheduleCompat(scheduleMillis, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent)

                sharedPreferenceHelper.saveRegistrationRemindTimestamp(scheduleMillis)
            } finally {
                registrationRemindHandling.set(false)
            }
        }
    }
}
