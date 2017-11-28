package org.stepic.droid.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.preferences.SharedPreferenceHelper
import java.io.IOException
import javax.inject.Inject

class StepicFcmListenerService : FirebaseMessagingService() {

    val hacker = HackFcmListener()

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val notificationRawString: String? = data?.get("object")
        try {
            val userId: Long? = hacker.sharedPreferenceHelper.profile?.id
            val userIdServerString = data?.get("user_id") ?: ""
            val userIdServer = Integer.parseInt(userIdServerString)
            if (userId == null || userIdServer.toLong() != userId) {
                return;
            }
            val stepicNotification = Gson().fromJson(notificationRawString, Notification::class.java)
            stepicNotification?.let {
                hacker.stepikNotificationManager.showNotification(it)
            }
        } catch (e: IOException) {
            //internet is not available. just ignore, we can't show reach notification
        } catch (e: Exception) {
            hacker.analytic.reportError(Analytic.Error.NOTIFICATION_ERROR_PARSE, e);
        }
    }

}

class HackFcmListener() {
    @Inject
    lateinit var stepikNotificationManager: StepikNotificationManager

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var analytic: Analytic

    init {
        App.component().inject(this)
    }
}