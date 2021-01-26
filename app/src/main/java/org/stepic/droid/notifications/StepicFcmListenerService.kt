package org.stepic.droid.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.stepic.droid.BuildConfig
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.core.StepikDevicePoster
import org.stepic.droid.notifications.badges.NotificationsBadgesManager
import org.stepic.droid.notifications.handlers.RemoteMessageHandler
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationStatuses
import org.stepic.droid.notifications.model.StepikNotificationChannel
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.ui.activities.MainFeedActivity
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.domain.story_deeplink.model.StoryDeepLinkNotification
import org.stepik.android.view.notification.FcmNotificationHandler
import java.io.IOException
import javax.inject.Inject

class StepicFcmListenerService : FirebaseMessagingService() {
    companion object {
        private const val NOTIFICATION_TYPE = "notifications" // todo: refactor in message handlers
        private const val NOTIFICATION_STATUSES_TYPE = "notification-statuses"
        private const val STORY_TEMPLATES = "story-templates"
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
                    STORY_TEMPLATES -> processStoryTemplatesDeeplink(rawMessageObject)
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
            hacker.fcmNotificationHandler.showNotification(it)
        }
    }

    private fun processNotificationStatuses(rawMessageObject: String?) {
        val notificationStatuses = Gson().fromJson(rawMessageObject, NotificationStatuses::class.java)
        notificationStatuses?.let {
            hacker.notificationsBadgesManager.syncCounter()
        }
    }

    private fun processStoryTemplatesDeeplink(rawMessageObject: String?) {
        val storyDeepLinkNotification = Gson().fromJson(rawMessageObject, StoryDeepLinkNotification::class.java)
        val requestCode = 1233

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(storyDeepLinkNotification.storyUrl))
            .setPackage(BuildConfig.APPLICATION_ID)

        val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(applicationContext)
        taskBuilder.addNextIntent(intent)

        val notification = NotificationCompat.Builder(applicationContext, StepikNotificationChannel.user.channelId)
            .setSmallIcon(R.drawable.ic_notification_icon_1)
            .setContentTitle(storyDeepLinkNotification.title)
            .setContentText(storyDeepLinkNotification.body)
            .setColor(applicationContext.resolveColorAttribute(R.attr.colorSecondary))
            .setAutoCancel(true)
            .setContentIntent(taskBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_ONE_SHOT))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notification.setStyle(NotificationCompat.BigTextStyle()
            .bigText(storyDeepLinkNotification.body))
            .setContentText(storyDeepLinkNotification.body)

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(requestCode, notification.build())
    }

    override fun onNewToken(token: String) {
        hacker.stepikDevicePoster.registerDevice()
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
    internal lateinit var fcmNotificationHandler: FcmNotificationHandler

    @Inject
    lateinit var stepikDevicePoster: StepikDevicePoster

    init {
        App.component().inject(this)
    }
}