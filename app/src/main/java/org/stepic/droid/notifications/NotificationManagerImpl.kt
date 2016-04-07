package org.stepic.droid.notifications

import android.annotation.TargetApi
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Looper
import android.support.annotation.DrawableRes
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.squareup.picasso.Picasso
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.R
import org.stepic.droid.base.MainApplication
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.model.Course
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationType
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.JsonHelper
import org.stepic.droid.view.activities.MainFeedActivity
import org.stepic.droid.web.IApi
import java.util.concurrent.atomic.AtomicInteger

class NotificationManagerImpl(val dbFacade: DatabaseFacade, val api: IApi, val configs: IConfig, val userPreferences: UserPreferences, val sharedPreferences: SharedPreferenceHelper) : INotificationManager {
    val GROUP_NOTIFICATION_KEY = "learn_notification"

    val notificationCounter: AtomicInteger = AtomicInteger()
    override fun showNotification(notification: Notification) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw RuntimeException("Can't create notification on main thread")
        }
        if (userPreferences.isNotificationEnabled) {
            resolveAndSendNotification(notification)
        } else {
            YandexMetrica.reportEvent("Notification is disabled by user in app")
        }
    }


    private fun resolveAndSendNotification(notification: Notification) {
        val htmlText = notification.htmlText
        if (htmlText == null || htmlText.isEmpty()) {
            YandexMetrica.reportEvent("notification html text was null", JsonHelper.toJson(notification))
        } else {
            //resolve which notification we should show
            when (notification.type) {
                NotificationType.learn -> sendLearnNotification(notification, htmlText, notification.id ?: 0)
                NotificationType.comments -> sendCommentNotification(notification, htmlText, notification.id ?: 0)
                else -> YandexMetrica.reportEvent("notification is not support: " + notification.type)
            }
        }
    }

    private fun sendCommentNotification(stepicNotification: Notification, rawMessageHtml: String, id: Long) {
        // just for test fixme: remove THIS!!! IMPLEMENT COMMENT
        YandexMetrica.reportEvent("notification comment is shown")
        sendLearnNotification(stepicNotification, rawMessageHtml, id)
    }

    private fun sendLearnNotification(stepicNotification: Notification, rawMessageHtml: String, id: Long) {
        YandexMetrica.reportEvent("notification learn is shown")

        val intent = Intent(MainApplication.getAppContext(), MainFeedActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(MainApplication.getAppContext(), 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT)


        val notificationNumber = notificationCounter.incrementAndGet()

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val largeIcon = getPictureByCourseId(HtmlHelper.parseCourseIdFromNotification(stepicNotification))
        val colorArgb = ColorUtil.getColorArgb(R.color.stepic_brand_primary)

        val title = MainApplication.getAppContext().getString(R.string.app_name)

        val justText: String = HtmlHelper.fromHtml(rawMessageHtml).toString()
        val notification = NotificationCompat.Builder(MainApplication.getAppContext())
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_notification_icon_1) // 1 is better
                .setContentTitle(title)
                .setContentText(justText)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(justText))
                .setColor(colorArgb)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setGroup(GROUP_NOTIFICATION_KEY)
                .setNumber(notificationNumber)
                .setDeleteIntent(getDeleteIntent())
        addVibrationIfNeed(notification)

        if (Build.VERSION.SDK_INT >= 16) {
            buildForJellyBean(notification)
        }

        val notificationManager = MainApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationNumber - 1)
        notificationManager.notify(notificationNumber, notification.build())
    }
    private fun getDeleteIntent () : PendingIntent{
        val onNotificationDiscarded = Intent(MainApplication.getAppContext(), NotificationBroadcastReceiver::class.java);
        onNotificationDiscarded.action = AppConstants.NOTIFICATION_CANCELED
        return PendingIntent.getBroadcast(MainApplication.getAppContext(), 0, onNotificationDiscarded, PendingIntent.FLAG_CANCEL_CURRENT)
    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun buildForJellyBean(builder: NotificationCompat.Builder): Unit {
        // for some reason Notification.PRIORITY_DEFAULT doesn't show the counter
        builder.setPriority(NotificationCompat.PRIORITY_HIGH)
    }

    private fun getPictureByCourseId(courseId: Long?): Bitmap {
        //FIXME create special icon for notification placeholder ?? dp in mdpi
        @DrawableRes val notificationPlaceholder = R.drawable.ic_course_placeholder
        if (courseId == null){
            return BitmapFactory.decodeResource(MainApplication.getAppContext().getResources(), notificationPlaceholder);
        }
        var course: Course? = dbFacade.getCourseById(courseId, DatabaseFacade.Table.enrolled)
        if (course == null) {
            course = api.getCourse(courseId).execute()?.body()?.courses?.get(0)
        }

        val cover = course?.cover

        if (cover == null) {
            return BitmapFactory.decodeResource(MainApplication.getAppContext().getResources(), notificationPlaceholder);
        } else {
            return Picasso.with(MainApplication.getAppContext())
                    .load(configs.baseUrl + cover)
                    .resize(200, 200) //pixels
                    .placeholder(notificationPlaceholder)
                    .error(notificationPlaceholder)
                    .get()
        }
    }

    private fun addVibrationIfNeed(builder: NotificationCompat.Builder) {
        if (userPreferences.isVibrateNotificationEnabled) {
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        }
    }

    override fun discardAllNotifications() {
        notificationCounter.set(0)
    }
}