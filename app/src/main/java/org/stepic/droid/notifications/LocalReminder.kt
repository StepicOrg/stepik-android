package org.stepic.droid.notifications

import android.support.annotation.MainThread

interface LocalReminder {

    @MainThread
    fun remindAboutApp()

    @MainThread
    fun remindAboutApp(millis: Long? = null)

    @MainThread
    fun userChangeStateOfNotification()

}
