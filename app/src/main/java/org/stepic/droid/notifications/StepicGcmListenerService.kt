package org.stepic.droid.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import com.google.android.gms.gcm.GcmListenerService
import com.google.gson.Gson
import org.stepic.droid.R
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.view.activities.MainFeedActivity

class StepicGcmListenerService : GcmListenerService() {

    override fun onMessageReceived(from: String?, data: Bundle?) {
        val notificationRawString: String? = data?.getString("object")
        val stepicNotification = Gson().fromJson(notificationRawString, Notification::class.java)

        stepicNotification?.htmlText?.let {
            sendNotification(it)
        }
    }

    private fun sendNotification(message: String) {
        val intent = Intent(this, MainFeedActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_code);
        val colorArgb = ColorUtil.getColorArgb(R.color.stepic_brand_primary)
        val notification = NotificationCompat.Builder(this)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_matching)
                .setContentTitle("I handle this notification")
                .setContentText(HtmlHelper.fromHtml(message).toString())

                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(HtmlHelper.fromHtml(message).toString()))
                .setColor(colorArgb)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0 /* ID of notification */, notification.build())
    }
}