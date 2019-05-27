package org.stepic.droid.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.notifications.badges.NotificationsBadgesManager
import org.stepic.droid.notifications.handlers.RemoteMessageHandler
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationStatuses
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepik.android.view.notification.NotificationResolver
import java.io.IOException
import javax.inject.Inject

class StepicFcmListenerService : FirebaseMessagingService() {
    companion object {
        private const val NOTIFICATION_TYPE = "notifications" // todo: refactor in message handlers
        private const val NOTIFICATION_STATUSES_TYPE = "notification-statuses"
    }

    private val hacker = HackFcmListener()

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            message.data?.let { data ->
                val userId: Long? = hacker.sharedPreferenceHelper.profile?.id
                val userIdServerString = data["user_id"] ?: ""
                val userIdServer = Integer.parseInt(userIdServerString)
                if (userId == null || userIdServer.toLong() != userId) {
                    return
                }

                val rawMessageObject = data["object"]
                val messageType = data["type"]
                when (messageType) {
                    NOTIFICATION_TYPE -> processNotification(rawMessageObject)
                    NOTIFICATION_STATUSES_TYPE -> processNotificationStatuses(rawMessageObject)
                    else -> hacker.handlers[messageType]?.handleMessage(this, rawMessageObject)
                }
            }
        } catch (e: IOException) {
            //internet is not available. just ignore, we can't show reach notification
        } catch (e: Exception) {
            hacker.analytic.reportError(Analytic.Error.NOTIFICATION_ERROR_PARSE, e)
        }
    }

    private fun processNotification(rawMessageObject: String?) {
        val stepikNotification = Gson().fromJson(rawMessageObject, Notification::class.java)
        stepikNotification?.let {
            hacker.notificationResolver.showNotification(it)
        }
    }

    private fun processNotificationStatuses(rawMessageObject: String?) {
        val notificationStatuses = Gson().fromJson(rawMessageObject, NotificationStatuses::class.java)
        notificationStatuses?.let {
            hacker.notificationsBadgesManager.syncCounter()
        }
    }

}

class HackFcmListener {

    @Inject
    lateinit var sharedPreferenceHelper: SharedPreferenceHelper

    @Inject
    lateinit var analytic: Analytic

    @Inject
    lateinit var notificationsBadgesManager: NotificationsBadgesManager

    @Inject
    internal lateinit var handlers: Map<String, @JvmSuppressWildcards RemoteMessageHandler>

    @Inject
    internal lateinit var notificationResolver: NotificationResolver

    init {
        App.component().inject(this)
    }
}