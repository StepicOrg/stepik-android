package org.stepic.droid.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import org.stepic.droid.R
import org.stepic.droid.notifications.model.StepikNotificationChannel

object NotificationChannelInitializer {
    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //channels were introduced only in O. Before we had used in-app channels
            return
        }

        val stepikNotificationChannels = StepikNotificationChannel.values()
        val androidChannels = ArrayList<NotificationChannel>(stepikNotificationChannels.size)
        stepikNotificationChannels.forEach {
            androidChannels.add(initChannel(context, it))
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannels(androidChannels)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initChannel(
        context: Context,
        stepikChannel: StepikNotificationChannel
    ): NotificationChannel {
        val channelName = context.getString(stepikChannel.visibleChannelNameRes)
        val channel = NotificationChannel(stepikChannel.channelId, channelName, stepikChannel.importance)
        channel.description = context.getString(stepikChannel.visibleChannelDescriptionRes)
        channel.enableLights(true)
        channel.enableVibration(true)
        channel.lightColor = context.resolveColorAttribute(R.attr.colorError)
        return channel
    }
}
