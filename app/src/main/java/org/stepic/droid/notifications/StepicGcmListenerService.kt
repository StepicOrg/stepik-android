package org.stepic.droid.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import com.google.android.gms.gcm.GcmListenerService
import com.google.gson.Gson
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.R
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.view.activities.MainFeedActivity

class StepicGcmListenerService : GcmListenerService() {

    override fun onMessageReceived(from: String?, data: Bundle?) {
        val emptyBundle = data!!.getBundle("notification")
        val notificationRawString : String = data!!.getString("object")
        val stepicNotification =  Gson().fromJson(notificationRawString, Notification::class.java)

        val message = data!!.getString("gcm.notification.title")
        YandexMetrica.reportEvent("gcm.notification.title is " + message);
        sendNotification("ETO CUSTOM " + (message ?: "null"))
    }


    /**
     * Create and show a simple notification containing the received GCM message.

     * @param message GCM message received.
     */
    private fun sendNotification(message: String) {
        val intent = Intent(this, MainFeedActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.stepic_logo)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0 /* ID of notification */, notification)
    }
}