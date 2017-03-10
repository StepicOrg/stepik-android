package org.stepic.droid.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import org.stepic.droid.base.App
import org.stepic.droid.notifications.INotificationManager
import javax.inject.Inject

class StreakAlarmService : IntentService("StreakAlarm") {

    companion object {
        var requestCode = 178
    }

    @Inject
    lateinit var notificationManager: INotificationManager

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        App.component().inject(this)
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

    override fun onHandleIntent(intent: Intent?) {
        notificationManager.showStreakRemind()
    }

}

