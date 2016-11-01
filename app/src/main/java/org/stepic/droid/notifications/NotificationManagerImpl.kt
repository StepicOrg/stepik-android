package org.stepic.droid.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.support.annotation.DrawableRes
import android.support.annotation.MainThread
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.bumptech.glide.Glide
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.MainApplication
import org.stepic.droid.configuration.IConfig
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.model.Course
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationType
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.store.operations.DatabaseFacade
import org.stepic.droid.store.operations.Table
import org.stepic.droid.ui.activities.SectionActivity
import org.stepic.droid.ui.activities.StepsActivity
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.resolvers.text.TextResolver
import org.stepic.droid.web.IApi

class NotificationManagerImpl(val sharedPreferenceHelper: SharedPreferenceHelper,
                              val api: IApi,
                              val configs: IConfig,
                              val userPreferences: UserPreferences,
                              val databaseFacade: DatabaseFacade,
                              val analytic: Analytic,
                              val textResolver: TextResolver,
                              val screenManager: ScreenManager) : INotificationManager {

    val issuedCertificateActionValue = "issued_certificate"

    override fun showNotification(notification: Notification) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw RuntimeException("Can't create notification on main thread")
        }

        if (!userPreferences.isNotificationEnabled) {
            analytic.reportEvent(Analytic.Notification.DISABLED_BY_USER)
        } else if (!sharedPreferenceHelper.isGcmTokenOk) {
            analytic.reportEvent(Analytic.Notification.GCM_TOKEN_NOT_OK)
        } else {
            resolveAndSendNotification(notification)
        }
    }

    private fun resolveAndSendNotification(notification: Notification) {
        val htmlText = notification.htmlText

        if (!NotificationHelper.isNotificationValidByAction(notification)) {
            analytic.reportEventWithIdName(Analytic.Notification.ACTION_NOT_SUPPORT, notification.id.toString(), notification.action ?: "")
            return
        } else if (htmlText == null || htmlText.isEmpty()) {
            analytic.reportEvent(Analytic.Notification.HTML_WAS_NULL, notification.id.toString())
            return
        } else if (notification.isMuted ?: false) {
            analytic.reportEvent(Analytic.Notification.WAS_MUTED, notification.id.toString())
            return
        } else {
            //resolve which notification we should show
            when (notification.type) {
                NotificationType.learn -> sendLearnNotification(notification, htmlText, notification.id ?: 0)
                NotificationType.comments -> sendCommentNotification(notification, htmlText, notification.id ?: 0)
                NotificationType.review -> sendReviewType(notification, htmlText, notification.id ?: 0)
                else -> analytic.reportEventWithIdName(Analytic.Notification.NOT_SUPPORT_TYPE, notification.id.toString(), notification.type.toString()) // it should never execute, because we handle it by action filter
            }
        }
    }

    private fun sendReviewType(stepikNotification: Notification, htmlText: String, id: Long) {
        // here is supportable action, but we need identify it
        val action = stepikNotification.action
        if (action != null && action == NotificationHelper.REVIEW_TAKEN) {
            val title = MainApplication.getAppContext().getString(R.string.received_review_title)
            val justText: String = textResolver.fromHtml(htmlText).toString()

            val link = HtmlHelper.parseLinkToLessonFromNotification(htmlText, configs.baseUrl) ?: ""
            val intent = getReviewIntent(link)
//            intent.action = AppConstants.OPEN_NOTIFICATION //FIXME HANDLE OPEN NOTIFICATION IN LESSON FOR CHECK SHOWN

            val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(MainApplication.getAppContext())
            taskBuilder.addParentStack(StepsActivity::class.java)
            taskBuilder.addNextIntent(intent)

            showSimpleNotification(id, justText, taskBuilder, title)
        }
    }


    private fun sendCommentNotification(stepicNotification: Notification, htmlText: String, id: Long) {
        val action = stepicNotification.action
        if (action != null && (action == NotificationHelper.REPLIED || action == NotificationHelper.COMMENTED)) {
            val title = MainApplication.getAppContext().getString(R.string.new_message_title)
            val justText: String = textResolver.fromHtml(htmlText).toString()

            val link: String?
            if (action == NotificationHelper.REPLIED) {
                link = HtmlHelper.parseNLinkInText(htmlText, configs.baseUrl, 1)
            } else {
                link = HtmlHelper.parseNLinkInText(htmlText, configs.baseUrl, 3)
            }

            if (link == null) {
                analytic.reportEvent(Analytic.Notification.CANT_PARSE_NOTIFICATION, id.toString())
                return
            }

            val intent = getCommentIntent(link)
//            intent.action = AppConstants.OPEN_NOTIFICATION //FIXME HANDLE OPEN NOTIFICATION IN LESSON FOR CHECK SHOWN

            val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(MainApplication.getAppContext())
            taskBuilder.addParentStack(StepsActivity::class.java)
            taskBuilder.addNextIntent(intent)

            analytic.reportEventWithIdName(Analytic.Notification.NOTIFICATION_SHOWN, id.toString(), stepicNotification.type?.name)
            showSimpleNotification(id, justText, taskBuilder, title)
        }
    }

    private fun showSimpleNotification(id: Long, justText: String, taskBuilder: TaskStackBuilder, title: String?) {
        val pendingIntent = taskBuilder.getPendingIntent(id.toInt(), PendingIntent.FLAG_ONE_SHOT) //fixme if it will overlay courses id -> bug

        val colorArgb = ColorUtil.getColorArgb(R.color.stepic_brand_primary)
        val notification = NotificationCompat.Builder(MainApplication.getAppContext())
                .setSmallIcon(R.drawable.ic_notification_icon_1)
                .setContentTitle(title)
                .setContentText(justText)
                .setColor(colorArgb)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(getDeleteIntent())
        addVibrationIfNeed(notification)
        addSoundIfNeed(notification)

        notification.setStyle(NotificationCompat.BigTextStyle()
                .bigText(justText))
                .setContentText(justText)
                .setNumber(1)
        val notificationManager = MainApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id.toInt(), notification.build())
    }

    private fun sendLearnNotification(stepicNotification: Notification, rawMessageHtml: String, id: Long) {
        val courseId: Long = HtmlHelper.parseCourseIdFromNotification(stepicNotification) ?: 0L
        if (courseId == 0L) {
            analytic.reportEvent(Analytic.Notification.CANT_PARSE_COURSE_ID, stepicNotification.id.toString())
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

        val largeIcon = getPictureByCourse(relatedCourse)
        val colorArgb = ColorUtil.getColorArgb(R.color.stepic_brand_primary)

        val intent = Intent(MainApplication.getAppContext(), SectionActivity::class.java)
        val bundle = Bundle()
        val modulePosition = HtmlHelper.parseModulePositionFromNotification(stepicNotification.htmlText)
        if (courseId >= 0 && modulePosition != null && modulePosition >= 0) {
            bundle.putLong(AppConstants.KEY_COURSE_LONG_ID, courseId)
            bundle.putInt(AppConstants.KEY_MODULE_POSITION, modulePosition)
        } else {
            bundle.putSerializable(AppConstants.KEY_COURSE_BUNDLE, relatedCourse)
        }
        intent.putExtras(bundle)
        intent.action = AppConstants.OPEN_NOTIFICATION
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(MainApplication.getAppContext())
        taskBuilder.addParentStack(SectionActivity::class.java)
        taskBuilder.addNextIntent(intent)

        val pendingIntent = taskBuilder.getPendingIntent(courseId.toInt(), PendingIntent.FLAG_ONE_SHOT)

        val title = MainApplication.getAppContext().getString(R.string.app_name)
        val justText: String = textResolver.fromHtml(rawMessageHtml).toString()

        val notification = NotificationCompat.Builder(MainApplication.getAppContext())
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_notification_icon_1) // 1 is better
                .setContentTitle(title)
                .setContentText(justText)
                .setColor(colorArgb)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(getDeleteIntent(courseId))
        addVibrationIfNeed(notification)
        addSoundIfNeed(notification)

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
                val line = textResolver.fromHtml(notificationItem?.htmlText ?: "").toString()
                inboxStyle.addLine(line);
            }
            inboxStyle.setSummaryText(summaryText)
            notification.setStyle(inboxStyle)
                    .setNumber(numberOfNotification)
        }

        analytic.reportEventWithIdName(Analytic.Notification.NOTIFICATION_SHOWN, stepicNotification.id?.toString() ?: "", stepicNotification.type?.name)
        analytic.reportEvent(Analytic.Notification.LEARN_SHOWN)
        val notificationManager = MainApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(courseId.toInt(), notification.build())
    }

    private fun getDeleteIntent(courseId: Long = -1): PendingIntent {
        val onNotificationDiscarded = Intent(MainApplication.getAppContext(), NotificationBroadcastReceiver::class.java);
        onNotificationDiscarded.action = AppConstants.NOTIFICATION_CANCELED
        val bundle = Bundle()
        if (courseId < 0) {
            bundle.putSerializable(AppConstants.COURSE_ID_KEY, courseId)
        }
        onNotificationDiscarded.putExtras(bundle)
        //add course id for bundle
        return PendingIntent.getBroadcast(MainApplication.getAppContext(), 0, onNotificationDiscarded, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun getCourse(courseId: Long?): Course? {
        if (courseId == null) return null
        var course: Course? = databaseFacade.getCourseById(courseId, Table.enrolled)
        if (course == null) {
            course = api.getCourse(courseId).execute()?.body()?.courses?.get(0)
        }
        return course
    }

    private fun getPictureByCourse(course: Course?): Bitmap {
        val cover = course?.cover
        @DrawableRes val notificationPlaceholder = R.drawable.ic_course_placeholder
        if (cover == null) {
            return getBitmap(R.drawable.ic_course_placeholder)
        } else {
            return Glide.with(MainApplication.getAppContext())
                    .load(configs.baseUrl + cover)
                    .asBitmap()
                    .placeholder(notificationPlaceholder)
                    .into(200, 200)//pixels
                    .get()
        }
    }

    private fun getBitmap(@DrawableRes drawable: Int): Bitmap {
        return BitmapFactory.decodeResource(MainApplication.getAppContext().resources, drawable);
    }

    private fun addVibrationIfNeed(builder: NotificationCompat.Builder) {
        if (userPreferences.isVibrateNotificationEnabled) {
            builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE)
        }
    }

    private fun addSoundIfNeed(builder: NotificationCompat.Builder) {
        if (userPreferences.isSoundNotificationEnabled) {
            val stepicSound = Uri.parse("android.resource://"
                    + MainApplication.getAppContext().getPackageName() + "/" + R.raw.default_sound);
            builder.setSound(stepicSound)
        }
    }

    override fun discardAllNotifications(courseId: Long) {
        databaseFacade.removeAllNotificationsByCourseId(courseId)
    }

    @MainThread
    override fun tryOpenNotificationInstantly(notification: Notification) {
        val isShown = when (notification.type) {
            NotificationType.learn -> openLearnNotification(notification)
            NotificationType.comments -> openCommentNotification(notification)
            NotificationType.review -> openReviewNotification(notification)
            NotificationType.teach -> openTeach(notification)
            NotificationType.default -> openDefault(notification)
            null -> false
        }

        if (!isShown) {
            analytic.reportEvent(Analytic.Notification.NOTIFICATION_NOT_OPENABLE, notification.action ?: "")
        }

    }

    private fun openTeach(notification: Notification): Boolean {
        return false
    }

    private fun openDefault(notification: Notification): Boolean {
        if (notification.action != null && notification.action == "added_to_group") {
            val data = HtmlHelper.parseLinkToCourseFromDefaultNotifiation(notification.htmlText ?: "", configs.baseUrl) ?: return false
            val intent = Intent(MainApplication.getAppContext(), SectionActivity::class.java)
            intent.data = Uri.parse(data)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            analytic.reportEvent(Analytic.Notification.OPEN_COMMENT_NOTIFICATION_LINK)
            MainApplication.getAppContext().startActivity(intent)
            return true
        } else {
            return false
        }
    }

    @MainThread
    private fun openReviewNotification(notification: Notification): Boolean {
        val data = HtmlHelper.parseLinkToLessonFromNotification(notification.htmlText ?: "", configs.baseUrl) ?: return false
        val intent = getReviewIntent(data)
        MainApplication.getAppContext().startActivity(intent)
        analytic.reportEvent(Analytic.Notification.OPEN_LESSON_NOTIFICATION_LINK)
        return true
    }

    private fun getReviewIntent(link: String): Intent {
        val intent = Intent(MainApplication.getAppContext(), StepsActivity::class.java)
        intent.data = Uri.parse(link)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent
    }

    @MainThread
    private fun openCommentNotification(notification: Notification): Boolean {
        val link: String
        val action = notification.action
        val htmlText = notification.htmlText ?: ""
        if (action == NotificationHelper.REPLIED) {
            link = HtmlHelper.parseNLinkInText(htmlText, configs.baseUrl, 1) ?: return false
        } else {
            link = HtmlHelper.parseNLinkInText(htmlText, configs.baseUrl, 3) ?: return false
        }
        val intent = getCommentIntent(link)
        analytic.reportEvent(Analytic.Notification.OPEN_COMMENT_NOTIFICATION_LINK)
        MainApplication.getAppContext().startActivity(intent)
        return true
    }

    private fun getCommentIntent(link: String): Intent {
        val intent = Intent(MainApplication.getAppContext(), StepsActivity::class.java)
        intent.data = Uri.parse(link)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent
    }

    @MainThread
    private fun openLearnNotification(notification: Notification): Boolean {
        if (notification.action != null && notification.action == issuedCertificateActionValue) {
            analytic.reportEvent(Analytic.Certificate.OPEN_CERTIFICATE_FROM_NOTIFICATION_CENTER)
            screenManager.showCertificates()
            return true
        } else {
            val courseId = HtmlHelper.parseCourseIdFromNotification(notification)
            val modulePosition = HtmlHelper.parseModulePositionFromNotification(notification.htmlText)

            if (courseId != null && courseId >= 0 && modulePosition != null && modulePosition >= 0) {
                val intent = Intent(MainApplication.getAppContext(), SectionActivity::class.java)
                val bundle = Bundle()
                bundle.putLong(AppConstants.KEY_COURSE_LONG_ID, courseId)
                bundle.putInt(AppConstants.KEY_MODULE_POSITION, modulePosition)
                intent.putExtras(bundle)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainApplication.getAppContext().startActivity(intent)
                return true
            } else {
                return false
            }
        }
    }
}