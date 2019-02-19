package org.stepic.droid.notifications

import android.support.annotation.AnyThread
import android.support.annotation.MainThread

interface LocalReminder {

    @MainThread
    fun remindAboutApp()

    @MainThread
    fun remindAboutApp(millis: Long? = null)

    @MainThread
    fun userChangeStateOfNotification()

    @AnyThread
    fun remindAboutRegistration()

    @AnyThread
    fun scheduleRetentionNotification(shouldResetCounter: Boolean = true)

}
