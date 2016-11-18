package org.stepic.droid.services

import android.app.IntentService
import android.content.Intent

class NewUserAlarmService : IntentService("NewUserAlarm") {
    companion object {
        var notificationTimestampSentKey = "notificationTimestampKey"
        var requestCode = 177
    }

    override fun onHandleIntent(intent: Intent?) {

    }
}
