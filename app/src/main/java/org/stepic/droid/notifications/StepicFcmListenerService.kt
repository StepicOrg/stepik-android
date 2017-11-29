package org.stepic.droid.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.notifications.badges.NotificationsBadgesManager
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationStatuses
import org.stepic.droid.preferences.SharedPreferenceHelper
import java.io.IOException
import javax.inject.Inject

class StepicFcmListenerService : FirebaseMessagingService() {
    companion object {
        private const val NOTIFICATION_TYPE = "notifications"
        private const val NOTIFICATION_STATUSES_TYPE ="notification-statuses"
    }

    private val hacker = HackFcmListener()

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            message.data?.let { data ->
                Log.d(javaClass.canonicalName, data.toString())
                val userId: Long? = hacker.sharedPreferenceHelper.profile?.id
                val userIdServerString = data["user_id"] ?: ""
                val userIdServer = Integer.parseInt(userIdServerString)
                if (userId == null || userIdServer.toLong() != userId) {
                    return
                }

                val rawMessageObject = data["object"]
                when (data["type"]) {
                    NOTIFICATION_TYPE -> processNotification(rawMessageObject)
                    NOTIFICATION_STATUSES_TYPE -> processNotificationStatuses(rawMessageObject, message.sentTime)
                }
            }
        } catch (e: IOException) {
            //internet is not available. just ignore, we can't show reach notification
        } catch (e: Exception) {
            hacker.analytic.reportError(Analytic.Error.NOTIFICATION_ERROR_PARSE, e)
        }
    }

    private fun processNotification(rawMessageObject: String?) {
        val stepicNotification = Gson().fromJson(rawMessageObject, Notification::class.java)
        stepicNotification?.let {
            hacker.stepikNotificationManager.showNotification(it)
        }
    }

    private fun processNotificationStatuses(rawMessageObject: String?, timestamp: Long) {
        val notificationStatuses = Gson().fromJson(rawMessageObject, NotificationStatuses::class.java)
        notificationStatuses?.let {
            hacker.notificationsBadgesManager.setCounter(it, timestamp)
        }
    }

}

class HackFcmListener {
    @Inject
    lateinit var stepikNotificationManager: StepikNotificationManager

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var notificationsBadgesManager: NotificationsBadgesManager

    init {
        App.component().inject(this)
    }
}