package org.stepic.droid.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import org.stepic.droid.base.App
import org.stepic.droid.notifications.NotificationManager
import javax.inject.Inject

class NewUserAlarmService : IntentService("NewUserAlarm") {
    companion object {
        var notificationTimestampSentKey = "notificationTimestampKey"
        var requestCode = 177
    }

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        App.component().inject(this)
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

    override fun onHandleIntent(intent: Intent?) {
        notificationManager.showLocalNotificationRemind()
    }
}
