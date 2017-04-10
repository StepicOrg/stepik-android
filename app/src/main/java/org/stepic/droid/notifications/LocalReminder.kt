package org.stepic.droid.notifications

import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import org.stepic.droid.notifications.model.Notification

interface LocalReminder {

    @MainThread
    fun remindAboutApp()

    @MainThread
    fun remindAboutApp(millis: Long? = null)

    @MainThread
    fun userChangeStateOfNotification()

    @WorkerThread
    fun rescheduleNotification(stepikNotification: Notification)

}
