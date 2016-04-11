package org.stepic.droid.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Looper
import android.support.annotation.DrawableRes
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.squareup.picasso.Picasso
import com.yandex.metrica.YandexMetrica
import org.stepic.droid.R
import org.stepic.droid.base.MainApplication
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.model.Course
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationType
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.JsonHelper
import org.stepic.droid.view.activities.SectionActivity
import org.stepic.droid.web.IApi

class NotificationManagerImpl(val dbFacade: DatabaseFacade, val api: IApi, val configs: IConfig, val userPreferences: UserPreferences, val databaseFacade: DatabaseFacade) : INotificationManager {
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
        if (!NotificationHelper.isNotificationValidByAction(notification.action)) {
            YandexMetrica.reportEvent("notification action is not support", JsonHelper.toJson(notification))
            return
        } else if (htmlText == null || htmlText.isEmpty()) {
            YandexMetrica.reportEvent("notification html text was null", JsonHelper.toJson(notification))
            return
        } else if (notification.isMuted ?: false) {
            YandexMetrica.reportEvent("notification html text was muted", JsonHelper.toJson(notification))
            return
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
        //        YandexMetrica.reportEvent("notification comment is shown")
        //        sendLearnNotification(stepicNotification, rawMessageHtml, id)
    }

    private fun sendLearnNotification(stepicNotification: Notification, rawMessageHtml: String, id: Long) {
        YandexMetrica.reportEvent("notification learn is shown")

        val courseId: Long = HtmlHelper.parseCourseIdFromNotification(stepicNotification) ?: 0L
        if (courseId == 0L) {
            YandexMetrica.reportEvent("notification, cant parse courseId")
            return
        }
        stepicNotification.course_id = courseId
        val notificationOfCourseList: MutableList<Notification?> = databaseFacade.getAllNotificationsOfCourse(courseId)
        val relatedCourse = getCourse(courseId)
        var isNeedAdd = true
        for (notificationItem in notificationOfCourseList) {
            if (notificationItem?.id == stepicNotification.id) {
                isNeedAdd = false
                break
            }
        }

        if (isNeedAdd) {
            notificationOfCourseList.add(stepicNotification)
            databaseFacade.addNotification(stepicNotification)
        }


        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val largeIcon = getPictureByCourse(relatedCourse)
        val colorArgb = ColorUtil.getColorArgb(R.color.stepic_brand_primary)

        val intent = Intent(MainApplication.getAppContext(), SectionActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable(AppConstants.KEY_COURSE_BUNDLE, relatedCourse)
        intent.putExtras(bundle)
        intent.action = AppConstants.OPEN_NOTIFICATION
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(MainApplication.getAppContext())
        taskBuilder.addParentStack(SectionActivity::class.java)
        taskBuilder.addNextIntent(intent)

        val pendingIntent = taskBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT)

        val title = MainApplication.getAppContext().getString(R.string.app_name)
        val justText: String = HtmlHelper.fromHtml(rawMessageHtml).toString()

        val notification = NotificationCompat.Builder(MainApplication.getAppContext())
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_notification_icon_1) // 1 is better
                .setContentTitle(title)
                .setContentText(justText)
                .setColor(colorArgb)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(getDeleteIntent(courseId))
        addVibrationIfNeed(notification)

        val numberOfNotification = notificationOfCourseList.size
        val summaryText = MainApplication.getAppContext().getResources().getQuantityString(R.plurals.notification_plural, numberOfNotification, numberOfNotification)
        if (notificationOfCourseList.size == 1) {
            notification.setStyle(NotificationCompat.BigTextStyle()
                    .bigText(justText))
                    .setContentText(justText)
                    .setNumber(1)


        } else {
            val inboxStyle = NotificationCompat.InboxStyle()
            for (notificationItem in notificationOfCourseList.reversed()) {
                val line = HtmlHelper.fromHtml(notificationItem?.htmlText).toString()
                inboxStyle.addLine(line);
            }

            inboxStyle.setSummaryText(summaryText)

            notification.setStyle(inboxStyle)
                    .setNumber(numberOfNotification)
        }

        val notificationManager = MainApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(courseId.toInt(), notification.build())
    }

    private fun getDeleteIntent(courseId: Long): PendingIntent {
        val onNotificationDiscarded = Intent(MainApplication.getAppContext(), NotificationBroadcastReceiver::class.java);
        onNotificationDiscarded.action = AppConstants.NOTIFICATION_CANCELED
        val bundle = Bundle()
        bundle.putSerializable(AppConstants.COURSE_ID_KEY, courseId)
        onNotificationDiscarded.putExtras(bundle)
        //add course id for bundle
        return PendingIntent.getBroadcast(MainApplication.getAppContext(), 0, onNotificationDiscarded, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun getCourse(courseId: Long?): Course? {
        if (courseId == null) return null
        var course: Course? = dbFacade.getCourseById(courseId, DatabaseFacade.Table.enrolled)
        if (course == null) {
            course = api.getCourse(courseId).execute()?.body()?.courses?.get(0)
        }
        return course
    }

    private fun getPictureByCourse(course: Course?): Bitmap {
        val cover = course?.cover
        @DrawableRes val notificationPlaceholder = R.drawable.ic_course_placeholder
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

    override fun discardAllNotifications(courseId: Long) {
        databaseFacade.removeAllNotificationsByCourseId(courseId)
    }
}