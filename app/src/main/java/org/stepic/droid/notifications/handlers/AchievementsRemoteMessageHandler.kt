package org.stepic.droid.notifications.handlers

import android.content.Context
import org.stepic.droid.features.achievements.service.AchievementsNotificationService

class AchievementsRemoteMessageHandler : RemoteMessageHandler {
    companion object {
        const val MESSAGE_TYPE = "achievement-progresses"
    }

    override fun handleMessage(context: Context, rawMessage: String?) =
            AchievementsNotificationService.enqueueWork(context, rawMessage)
}