package org.stepik.android.view.notification.helpers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.bumptech.glide.Glide
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.configuration.Config
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.notifications.NotificationBroadcastReceiver
import org.stepic.droid.notifications.NotificationTimeChecker
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.StepikNotificationChannel
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.ui.activities.StepsActivity
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.HtmlHelper
import org.stepik.android.model.Course
import org.stepik.android.view.course.ui.activity.CourseActivity
import javax.inject.Inject

class NotificationHelperImpl
@Inject constructor(
    val context: Context,
    val configs: Config,
    val screenManager: ScreenManager,
    val notificationTimeChecker: NotificationTimeChecker,
    val analytic: Analytic,
    val userPreferences: UserPreferences
) : NotificationHelper {
    override fun makeSimpleNotificationBuilder(
        stepikNotification: Notification?,
        justText: String,
        taskBuilder: TaskStackBuilder,
        title: String?,
        deleteIntent: PendingIntent,
        id: Long
    ):  NotificationCompat.Builder {
        val pendingIntent = taskBuilder.getPendingIntent(id.toInt(), PendingIntent.FLAG_ONE_SHOT) // fixme if it will overlay courses id -> bug

        val colorArgb = ColorUtil.getColorArgb(R.color.stepic_brand_primary)
        val notification = NotificationCompat.Builder(context, stepikNotification?.type?.channel?.channelId ?: StepikNotificationChannel.user.channelId)
                .setSmallIcon(R.drawable.ic_notification_icon_1)
                .setContentTitle(title)
                .setContentText(justText)
                .setColor(colorArgb)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(deleteIntent)

        notification.setStyle(NotificationCompat.BigTextStyle()
                .bigText(justText))
                .setContentText(justText)

        // if notification is null (for example for streaks) -> show it always with sound and vibrate

        val isNight = notificationTimeChecker.isNight(DateTimeHelper.nowLocal())
        if (isNight) {
            analytic.reportEvent(Analytic.Notification.NIGHT_WITHOUT_SOUND_AND_VIBRATE)
        }

        if (stepikNotification == null || !isNight) {
            addVibrationIfNeed(notification)
            addSoundIfNeed(notification)
        }

        return notification
    }

    override fun getDeleteIntent(courseId: Long): PendingIntent {
        val intent = Intent(context, NotificationBroadcastReceiver::class.java)
        intent.action = AppConstants.NOTIFICATION_CANCELED
        val bundle = Bundle()
        if (courseId > 0) {
            bundle.putSerializable(AppConstants.COURSE_ID_KEY, courseId)
        }
        intent.putExtras(bundle)
        // add course id for bundle
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    override fun addVibrationIfNeed(builder: NotificationCompat.Builder) {
        if (userPreferences.isVibrateNotificationEnabled) {
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        }
    }

    override fun addSoundIfNeed(builder: NotificationCompat.Builder) {
        if (userPreferences.isSoundNotificationEnabled) {
            val stepicSound = Uri.parse("android.resource://" +
                    context.packageName + "/" + R.raw.default_sound)
            builder.setSound(stepicSound)
        }
    }

    override fun getTeachIntent(notification: Notification): Intent? {
        val link = HtmlHelper.parseNLinkInText(notification.htmlText ?: "", configs.baseUrl, 0) ?: return null
        try {
            val url = Uri.parse(link)
            val identifier = url.pathSegments[0]
            val intent: Intent =
                when (identifier) {
                    "course" ->
                        Intent(context, CourseActivity::class.java)
                    "lesson" ->
                        Intent(context, StepsActivity::class.java)
                    else ->
                        return null
                }
            intent.data = url
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        } catch (exception: Exception) {
            return null
        }
    }

    override fun getLicenseIntent(notification: Notification): Intent? {
        val link = HtmlHelper.parseNLinkInText(notification.htmlText ?: "", configs.baseUrl, 0) ?: return null
        val intent = screenManager.getOpenInWebIntent(link)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    override fun getDefaultIntent(notification: Notification): Intent? {
        val data = HtmlHelper.parseNLinkInText(notification.htmlText ?: "", configs.baseUrl, 1) ?: return null
        val intent = Intent(context, CourseActivity::class.java)
        intent.data = Uri.parse(data)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    override fun getReviewIntent(notification: Notification): Intent? {
        val data = HtmlHelper.parseNLinkInText(notification.htmlText ?: "", configs.baseUrl, 0) ?: return null
        val intent = Intent(context, StepsActivity::class.java)
        intent.data = Uri.parse(data)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    override fun getCommentIntent(notification: Notification): Intent? {
        val link: String
        val action = notification.action
        val htmlText = notification.htmlText ?: ""
        if (action == org.stepic.droid.notifications.NotificationHelper.REPLIED) {
            link = HtmlHelper.parseNLinkInText(htmlText, configs.baseUrl, 1) ?: return null
        } else {
            link = HtmlHelper.parseNLinkInText(htmlText, configs.baseUrl, 3) ?: return null
        }
        val intent = Intent(context, StepsActivity::class.java)
        intent.data = Uri.parse(link)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    override fun getPictureByCourse(course: Course?): Bitmap {
        val cover = course?.cover
        val notificationPlaceholder = R.drawable.general_placeholder

        return if (cover == null) {
            getBitmap(notificationPlaceholder)
        } else {
            try { // in order to suppress gai exception
                Glide.with(context)
                        .asBitmap()
                        .load(cover)
                        .placeholder(notificationPlaceholder)
                        .submit(200, 200) // pixels
                        .get()
            } catch (e: Exception) {
                getBitmap(notificationPlaceholder)
            }
        }
    }
    private fun getBitmap(@DrawableRes drawable: Int): Bitmap =
        BitmapFactory.decodeResource(context.resources, drawable)
}