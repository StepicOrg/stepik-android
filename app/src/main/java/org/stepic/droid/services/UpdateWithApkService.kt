package org.stepic.droid.services

import android.app.IntentService
import android.app.Service
import android.content.Intent
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.util.AppConstants

class UpdateWithApkService : IntentService("update_with_apk") {
    companion object {
        val linkKey = "LINK_KEY"
    }

    override fun onHandleIntent(intent: Intent?) {
        try {
            val linkFromServer = intent?.getStringExtra(linkKey)
            updateFromRemoteApk(path = linkFromServer!!)
        } catch (e: Exception) {
            YandexMetrica.reportError("update apk is failed", e)
        }

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_REDELIVER_INTENT
    }

    fun updateFromRemoteApk(path: String) {

    }
}
