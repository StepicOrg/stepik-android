package org.stepic.droid.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.Shell
import org.stepic.droid.notifications.model.Notification
import javax.inject.Inject

class StepicFcmListenerService : FirebaseMessagingService() {

    val hacker = HackFcmListener()

    override fun onMessageReceived(message : RemoteMessage) {
         val data = message.data
        val notificationRawString: String? = data?.get("object")
        try {
            val userId : Long? = hacker.shell.sharedPreferenceHelper.profile?.id
            val userIdServerString = data?.get("user_id")?:""
            val userIdServer = Integer.parseInt(userIdServerString)
            if (userId == null || userIdServer.toLong() != userId){
                return;
            }
            val stepicNotification = Gson().fromJson(notificationRawString, Notification::class.java)
            stepicNotification?.let {
                hacker.notificationManager.showNotification(it)
            }
        } catch(e: Exception) {
            hacker.analytic.reportError(Analytic.Error.NOTIFICATION_ERROR_PARSE, e);
        }
    }

}

class HackFcmListener() {
    @Inject
    lateinit var notificationManager: INotificationManager

    @Inject
    lateinit var shell: Shell

    @Inject
    lateinit var analytic : Analytic

    init {
        App.component().inject(this)
    }
}