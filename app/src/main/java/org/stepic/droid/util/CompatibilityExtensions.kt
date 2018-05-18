package org.stepic.droid.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.res.Configuration
import android.os.Build
import java.util.*

val Configuration.defaultLocale: Locale
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        locales.get(0)
    } else {
        @Suppress("DEPRECATION")
        locale
    }

fun AlarmManager.scheduleCompat(scheduleMillis: Long, interval: Long, pendingIntent: PendingIntent) {
    if (Build.VERSION.SDK_INT < 23) {
        if (Build.VERSION.SDK_INT >= 19) {
            setWindow(AlarmManager.RTC_WAKEUP, scheduleMillis, interval, pendingIntent)
        } else {
            set(AlarmManager.RTC_WAKEUP, scheduleMillis + interval / 2, pendingIntent)
        }
    } else {
        setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduleMillis + interval / 2, pendingIntent)
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, DateTime.now().millis + 15000, pendingIntent) //DEBUG PURPOSE ONLY
    }
}