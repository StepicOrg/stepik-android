package org.stepic.droid.notifications

import android.os.Bundle
import com.google.android.gms.gcm.GcmListenerService
import com.google.gson.Gson
import org.stepic.droid.base.MainApplication
import org.stepic.droid.notifications.model.Notification
import javax.inject.Inject

class StepicGcmListenerService : GcmListenerService() {

    @Inject
    lateinit var notificationManager: INotificationManager

    init {
        MainApplication.component().inject(this)
    }

    override fun onMessageReceived(from: String?, data: Bundle?) {
        val notificationRawString: String? = data?.getString("object")
        val stepicNotification = Gson().fromJson(notificationRawString, Notification::class.java)
        stepicNotification?.let {
            notificationManager.showNotification(it)
        }
    }

}