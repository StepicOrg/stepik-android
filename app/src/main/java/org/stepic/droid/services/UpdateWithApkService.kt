package org.stepic.droid.services

import android.app.IntentService
import android.app.Service
import android.content.Intent

class UpdateWithApkService : IntentService("update_with_apk") {
    companion object {
        val linkKey = "LINK_KEY"
    }

    override fun onHandleIntent(intent: Intent?) {

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

}
