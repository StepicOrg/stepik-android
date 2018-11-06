package org.stepic.droid.features.achievements.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.app.JobIntentService
import android.support.v4.app.NotificationCompat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.App
import org.stepic.droid.features.achievements.repository.AchievementsRepository
import org.stepic.droid.features.achievements.ui.activity.AchievementsListActivity
import org.stepic.droid.features.achievements.util.AchievementResourceResolver
import org.stepic.droid.model.AchievementFlatItem
import org.stepic.droid.model.AchievementNotification
import org.stepic.droid.notifications.model.StepikNotificationChannel
import org.stepic.droid.ui.util.toBitmap
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.svg.GlideSvgRequestFactory
import org.stepic.droid.util.toObject
import javax.inject.Inject

class AchievementsNotificationService : JobIntentService() {
    companion object {
        private const val NOTIFICATION_TAG = "achievement"

        private const val EXTRA_RAW_MESSAGE = "raw_message"
        private const val JOB_ID = 2002

        fun enqueueWork(context: Context, rawMessage: String?) {
            enqueueWork(context, AchievementsNotificationService::class.java, JOB_ID, Intent().putExtra(EXTRA_RAW_MESSAGE, rawMessage))
        }
    }

    @Inject
    internal lateinit var analytic: Analytic

    @Inject
    internal lateinit var achievementsRepository: AchievementsRepository

    @Inject
    internal lateinit var notificationManager: NotificationManager

    @Inject
    internal lateinit var achievementResourceResolver: AchievementResourceResolver

    init {
        App.component().inject(this)
    }

    override fun onHandleWork(intent: Intent) {
        try {
            val rawMessage = intent.getStringExtra(EXTRA_RAW_MESSAGE) ?: return
            val achievementNotification = rawMessage.toObject<AchievementNotification>()

            val achievement = achievementsRepository
                    .getAchievement(achievementNotification.user, achievementNotification.kind)
                    .blockingGet()

            val notificationIntent = AchievementsListActivity
                    .createIntent(this, achievementNotification.user, isMyProfile = true)

            val pendingIntent = PendingIntent
                    .getActivity(this, 0, notificationIntent, 0)

            val largeIcon = getAchievementImageBitmap(achievement)

            val notification = NotificationCompat.Builder(this, StepikNotificationChannel.user.channelId)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.achievement_notification_message,
                            achievementResourceResolver.resolveTitleForKind(achievement.kind)))
                    .setSmallIcon(R.drawable.ic_notification_icon_1)
                    .setLargeIcon(largeIcon)
                    .setContentIntent(pendingIntent)
                    .setColor(ColorUtil.getColorArgb(R.color.stepic_brand_primary))
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .build()

            notificationManager.notify(NOTIFICATION_TAG, achievementNotification.achievement, notification)
        } catch (e: Exception) {}
    }

    private fun getAchievementImageBitmap(achievement: AchievementFlatItem): Bitmap {
        val iconSize = resources.getDimension(R.dimen.notification_large_icon_size).toInt()
        return GlideSvgRequestFactory
                .create(this, null)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .load(Uri.parse(achievementResourceResolver.resolveAchievementIcon(achievement)))
                .placeholder(R.drawable.general_placeholder)
                .into(iconSize, iconSize)
                .get()
                .toBitmap(iconSize, iconSize)
    }

}