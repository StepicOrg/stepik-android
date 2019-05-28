package org.stepik.android.view.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.notifications.NotificationHelper
import org.stepic.droid.notifications.NotificationTimeChecker
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationType
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.ui.activities.StepsActivity
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.resolvers.text.TextResolver
import org.stepic.droid.web.Api
import org.stepik.android.model.Course
import org.stepik.android.view.course.routing.CourseScreenTab
import org.stepik.android.view.course.ui.activity.CourseActivity
import javax.inject.Inject

class NotificationResolverImpl
@Inject constructor(
    private val context: Context,
    private val screenManager: ScreenManager,
    private val analytic: Analytic,
    private val userPreferences: UserPreferences,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val textResolver: TextResolver,
    private val notificationHelper: org.stepik.android.view.notification.helpers.NotificationHelper,
    private val databaseFacade: DatabaseFacade,
    private val api: Api,
    private val notificationTimeChecker: NotificationTimeChecker,
    private val stepikNotificationManager: StepikNotificationManager
) : NotificationResolver {
    override fun showNotification(notification: Notification) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw RuntimeException("Can't create notification on main thread")
        }

        if (!userPreferences.isNotificationEnabled(notification.type)) {
            analytic.reportEventWithName(Analytic.Notification.DISABLED_BY_USER, notification.type?.name)
        } else if (!sharedPreferenceHelper.isGcmTokenOk) {
            analytic.reportEvent(Analytic.Notification.GCM_TOKEN_NOT_OK)
        } else {
            resolveAndSendNotification(notification)
        }
    }

    override fun tryOpenNotificationInstantly(notification: Notification) {
        val isShown = when (notification.type) {
            NotificationType.learn -> openLearnNotification(notification)
            NotificationType.comments -> openCommentNotification(notification)
            NotificationType.review -> openReviewNotification(notification)
            NotificationType.teach -> openTeach(notification)
            NotificationType.other -> openDefault(notification)
            null -> false
        }

        if (!isShown) {
            analytic.reportEvent(Analytic.Notification.NOTIFICATION_NOT_OPENABLE, notification.action ?: "")
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
            // resolve which notification we should show
            when (notification.type) {
                NotificationType.learn -> sendLearnNotification(notification, htmlText, notification.id ?: 0)
                NotificationType.comments -> sendCommentNotification(notification, htmlText, notification.id ?: 0)
                NotificationType.review -> sendReviewType(notification, htmlText, notification.id ?: 0)
                NotificationType.other -> sendDefaultNotification(notification, htmlText, notification.id ?: 0)
                NotificationType.teach -> sendTeachNotification(notification, htmlText, notification.id ?: 0)
                else -> analytic.reportEventWithIdName(Analytic.Notification.NOT_SUPPORT_TYPE, notification.id.toString(), notification.type.toString()) // it should never execute, because we handle it by action filter
            }
        }
    }

    private fun sendTeachNotification(stepikNotification: Notification, htmlText: String, id: Long) {
        val title = context.getString(R.string.teaching_title)
        val justText: String = textResolver.fromHtml(htmlText).toString()

        val intent = notificationHelper.getTeachIntent(notification = stepikNotification)
        if (intent == null) {
            analytic.reportEvent(Analytic.Notification.CANT_PARSE_NOTIFICATION, id.toString())
            return
        }

        val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        taskBuilder.addParentStack(CourseActivity::class.java)
        taskBuilder.addNextIntent(prepareNotificationIntent(intent, id))

        analytic.reportEventWithIdName(Analytic.Notification.NOTIFICATION_SHOWN, id.toString(), stepikNotification.type?.name)
        val notification = notificationHelper.makeSimpleNotificationBuilder(stepikNotification, justText, taskBuilder, title, id = id)
        stepikNotificationManager.showNotification(id, notification.build())
    }

    private fun sendDefaultNotification(stepikNotification: Notification, htmlText: String, id: Long) {
        val action = stepikNotification.action
        if (action != null && action == NotificationHelper.ADDED_TO_GROUP) {
            val title = context.getString(R.string.added_to_group_title)
            val justText: String = textResolver.fromHtml(htmlText).toString()

            val intent = notificationHelper.getDefaultIntent(notification = stepikNotification)
            if (intent == null) {
                analytic.reportEvent(Analytic.Notification.CANT_PARSE_NOTIFICATION, id.toString())
                return
            }

            val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
            taskBuilder.addParentStack(CourseActivity::class.java)
            taskBuilder.addNextIntent(prepareNotificationIntent(intent, id))

            val notification = notificationHelper.makeSimpleNotificationBuilder(stepikNotification, justText, taskBuilder, title, id = id)
            stepikNotificationManager.showNotification(id, notification.build())
            analytic.reportEventWithIdName(Analytic.Notification.NOTIFICATION_SHOWN, id.toString(), stepikNotification.type?.name)
        } else {
            analytic.reportEvent(Analytic.Notification.CANT_PARSE_NOTIFICATION, id.toString())
        }
    }

    private fun sendReviewType(stepikNotification: Notification, htmlText: String, id: Long) {
        // here is supportable action, but we need identify it
        val action = stepikNotification.action
        if (action != null && action == NotificationHelper.REVIEW_TAKEN) {
            val title = context.getString(R.string.received_review_title)
            val justText: String = textResolver.fromHtml(htmlText).toString()

            val intent = notificationHelper.getReviewIntent(notification = stepikNotification)
            if (intent == null) {
                analytic.reportEvent(Analytic.Notification.CANT_PARSE_NOTIFICATION, stepikNotification.id.toString())
                return
            }

            val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
            taskBuilder.addParentStack(StepsActivity::class.java)
            taskBuilder.addNextIntent(prepareNotificationIntent(intent, id))

            analytic.reportEventWithIdName(Analytic.Notification.NOTIFICATION_SHOWN, id.toString(), stepikNotification.type?.name)
            val notification = notificationHelper.makeSimpleNotificationBuilder(stepikNotification, justText, taskBuilder, title, id = id)
            stepikNotificationManager.showNotification(id, notification.build())
        } else {
            analytic.reportEvent(Analytic.Notification.CANT_PARSE_NOTIFICATION, id.toString())
        }
    }

    private fun sendCommentNotification(stepikNotification: Notification, htmlText: String, id: Long) {
        val action = stepikNotification.action
        if (action != null && (action == NotificationHelper.REPLIED || action == NotificationHelper.COMMENTED)) {
            val title = context.getString(R.string.new_message_title)
            val justText: String = textResolver.fromHtml(htmlText).toString()

            val intent = notificationHelper.getCommentIntent(stepikNotification)
            if (intent == null) {
                analytic.reportEvent(Analytic.Notification.CANT_PARSE_NOTIFICATION, id.toString())
                return
            }

            val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
            taskBuilder.addParentStack(StepsActivity::class.java)
            taskBuilder.addNextIntent(prepareNotificationIntent(intent, id))

            analytic.reportEventWithIdName(Analytic.Notification.NOTIFICATION_SHOWN, id.toString(), stepikNotification.type?.name)
            val notification = notificationHelper.makeSimpleNotificationBuilder(stepikNotification, justText, taskBuilder, title, id = id)
            stepikNotificationManager.showNotification(id, notification.build())
        } else {
            analytic.reportEvent(Analytic.Notification.CANT_PARSE_NOTIFICATION, id.toString())
        }
    }

    private fun sendLearnNotification(stepikNotification: Notification, rawMessageHtml: String, id: Long) {
        val action = stepikNotification.action
        if (action != null && action == NotificationHelper.ISSUED_CERTIFICATE) {
            val title = context.getString(R.string.get_certifcate_title)
            val justText: String = textResolver.fromHtml(rawMessageHtml).toString()

            val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
            taskBuilder.addParentStack(StepsActivity::class.java)
            taskBuilder.addNextIntent(prepareNotificationIntent(screenManager.certificateIntent, id))
            analytic.reportEventWithIdName(Analytic.Notification.NOTIFICATION_SHOWN, id.toString(), stepikNotification.type?.name)
            val notification = notificationHelper.makeSimpleNotificationBuilder(stepikNotification, justText, taskBuilder, title, id = id)
            stepikNotificationManager.showNotification(id, notification.build())
        } else if (action == NotificationHelper.ISSUED_LICENSE) {
            val title = context.getString(R.string.get_license_message)
            val justText: String = textResolver.fromHtml(rawMessageHtml).toString()

            val intent = notificationHelper.getLicenseIntent(notification = stepikNotification) ?: return

            val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
            taskBuilder.addNextIntent(intent)

            analytic.reportEventWithIdName(Analytic.Notification.NOTIFICATION_SHOWN, id.toString(), stepikNotification.type.name)
            val notification = notificationHelper.makeSimpleNotificationBuilder(stepikNotification, justText, taskBuilder, title, id = id)
            stepikNotificationManager.showNotification(id, notification.build())
        } else {
            val courseId: Long = HtmlHelper.parseCourseIdFromNotification(stepikNotification) ?: 0L
            if (courseId == 0L) {
                analytic.reportEvent(Analytic.Notification.CANT_PARSE_COURSE_ID, stepikNotification.id.toString())
                return
            }
            stepikNotification.courseId = courseId
            val notificationOfCourseList: MutableList<Notification?> = databaseFacade.getAllNotificationsOfCourse(courseId).toMutableList()
            val relatedCourse = getCourse(courseId) ?: return
            val isNeedAdd = notificationOfCourseList.none { it?.id == stepikNotification.id }

            if (isNeedAdd) {
                notificationOfCourseList.add(stepikNotification)
                databaseFacade.addNotification(stepikNotification)
            }

            val largeIcon = notificationHelper.getPictureByCourse(relatedCourse)
            val colorArgb = ColorUtil.getColorArgb(R.color.stepic_brand_primary)

            val modulePosition = HtmlHelper.parseModulePositionFromNotification(stepikNotification.htmlText)
            val intent =
                if (courseId >= 0 && modulePosition != null && modulePosition >= 0) {
                    CourseActivity.createIntent(context, courseId, tab = CourseScreenTab.SYLLABUS)
                } else {
                    CourseActivity.createIntent(context, relatedCourse, tab = CourseScreenTab.SYLLABUS)
                }
            intent.action = AppConstants.OPEN_NOTIFICATION_FOR_CHECK_COURSE
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
            taskBuilder.addParentStack(CourseActivity::class.java)
            taskBuilder.addNextIntent(intent)

            val pendingIntent = taskBuilder.getPendingIntent(courseId.toInt(), PendingIntent.FLAG_ONE_SHOT)

            val title = context.getString(R.string.app_name)
            val justText: String = textResolver.fromHtml(rawMessageHtml).toString()

            val notification = NotificationCompat
                    .Builder(context, stepikNotification.type.channel.channelId)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.ic_notification_icon_1) // 1 is better
                    .setContentTitle(title)
                    .setContentText(justText)
                    .setColor(colorArgb)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setDeleteIntent(notificationHelper.getDeleteIntent(courseId))

            val numberOfNotification = notificationOfCourseList.size
            val summaryText = context.resources.getQuantityString(R.plurals.notification_plural, numberOfNotification, numberOfNotification)
            if (notificationOfCourseList.size == 1) {
                notification.setStyle(
                    NotificationCompat.BigTextStyle()
                            .bigText(justText))
                        .setContentText(justText)
                        .setNumber(1)
            } else {
                val inboxStyle = NotificationCompat.InboxStyle()
                for (notificationItem in notificationOfCourseList.reversed()) {
                    val line = textResolver.fromHtml(notificationItem?.htmlText ?: "").toString()
                    inboxStyle.addLine(line)
                }
                inboxStyle.setSummaryText(summaryText)
                notification.setStyle(inboxStyle)
                        .setNumber(numberOfNotification)
            }

            if (notificationTimeChecker.isNight(DateTimeHelper.nowLocal())) {
                analytic.reportEvent(Analytic.Notification.NIGHT_WITHOUT_SOUND_AND_VIBRATE)
            } else {
                notificationHelper.addVibrationIfNeed(notification)
                notificationHelper.addSoundIfNeed(notification)
            }

            analytic.reportEventWithIdName(Analytic.Notification.NOTIFICATION_SHOWN, stepikNotification.id?.toString() ?: "", stepikNotification.type.name)
            stepikNotificationManager.showNotification(courseId, notification.build())
        }
    }

    private fun openLearnNotification(notification: Notification): Boolean {
        if (notification.action != null && notification.action == NotificationHelper.ISSUED_CERTIFICATE) {
            analytic.reportEvent(Analytic.Certificate.OPEN_CERTIFICATE_FROM_NOTIFICATION_CENTER)
            screenManager.showCertificates(context)
            return true
        } else if (notification.action == NotificationHelper.ISSUED_LICENSE) {
            val intent: Intent = notificationHelper.getLicenseIntent(notification) ?: return false
            context.startActivity(intent)
            return true
        } else {
            val courseId = HtmlHelper.parseCourseIdFromNotification(notification)
            val modulePosition = HtmlHelper.parseModulePositionFromNotification(notification.htmlText)

            if (courseId != null && courseId >= 0 && modulePosition != null && modulePosition >= 0) {
                val intent = CourseActivity.createIntent(context, courseId, tab = CourseScreenTab.SYLLABUS) // Intent(context, SectionActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } else {
                return false
            }
        }
    }

    private fun openCommentNotification(notification: Notification): Boolean {
        val intent: Intent = notificationHelper.getCommentIntent(notification) ?: return false
        analytic.reportEvent(Analytic.Notification.OPEN_COMMENT_NOTIFICATION_LINK)
        context.startActivity(intent)
        return true
    }

    private fun openReviewNotification(notification: Notification): Boolean {
        val intent = notificationHelper.getReviewIntent(notification) ?: return false
        context.startActivity(intent)
        analytic.reportEvent(Analytic.Notification.OPEN_LESSON_NOTIFICATION_LINK)
        return true
    }

    private fun openTeach(notification: Notification): Boolean {
        val intent: Intent? = notificationHelper.getTeachIntent(notification) ?: return false
        analytic.reportEvent(Analytic.Notification.OPEN_TEACH_CENTER)
        context.startActivity(intent)
        return true
    }

    private fun openDefault(notification: Notification): Boolean {
        if (notification.action != null && notification.action == NotificationHelper.ADDED_TO_GROUP) {
            val intent = notificationHelper.getDefaultIntent(notification) ?: return false
            analytic.reportEvent(Analytic.Notification.OPEN_COMMENT_NOTIFICATION_LINK)
            context.startActivity(intent)
            return true
        } else {
            return false
        }
    }

    private fun prepareNotificationIntent(intent: Intent, notificationId: Long) =
        intent.apply {
            action = AppConstants.OPEN_NOTIFICATION
            putExtra(AppConstants.KEY_NOTIFICATION_ID, notificationId)
        }

    private fun getCourse(courseId: Long?): Course? {
        if (courseId == null) return null
        var course: Course? = databaseFacade.getCourseById(courseId)
        if (course == null) {
            course = api.getCourse(courseId).execute()?.body()?.courses?.firstOrNull()
        }
        return course
    }
}