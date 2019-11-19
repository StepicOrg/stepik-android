package org.stepik.android.view.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.configuration.Config
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.notifications.NotificationActionsHelper
import org.stepic.droid.notifications.NotificationTimeChecker
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.notifications.model.NotificationType
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.HtmlHelper
import org.stepic.droid.util.resolvers.text.TextResolver
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.model.Course
import org.stepik.android.view.course.routing.CourseScreenTab
import org.stepik.android.view.course.ui.activity.CourseActivity
import org.stepik.android.view.lesson.ui.activity.LessonActivity
import org.stepik.android.view.notification.helpers.NotificationHelper
import javax.inject.Inject

class FcmNotificationHandlerImpl
@Inject
constructor(
    private val context: Context,
    private val configs: Config,
    private val screenManager: ScreenManager,
    private val analytic: Analytic,
    private val userPreferences: UserPreferences,
    private val sharedPreferenceHelper: SharedPreferenceHelper,
    private val textResolver: TextResolver,
    private val notificationHelper: NotificationHelper,
    private val databaseFacade: DatabaseFacade,
    private val courseRepository: CourseRepository,
    private val notificationTimeChecker: NotificationTimeChecker,
    private val stepikNotificationManager: StepikNotificationManager
) : FcmNotificationHandler {
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

        if (!NotificationActionsHelper.isNotificationValidByAction(notification)) {
            analytic.reportEventWithIdName(Analytic.Notification.ACTION_NOT_SUPPORT, notification.id.toString(), notification.action ?: "")
            return
        } else if (htmlText == null || htmlText.isEmpty()) {
            analytic.reportEvent(Analytic.Notification.HTML_WAS_NULL, notification.id.toString())
            return
        } else if (notification.isMuted == true) {
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

        val intent = getTeachIntent(notification = stepikNotification)
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
        if (action != null && action == NotificationActionsHelper.ADDED_TO_GROUP) {
            val title = context.getString(R.string.added_to_group_title)
            val justText: String = textResolver.fromHtml(htmlText).toString()

            val intent = getDefaultIntent(notification = stepikNotification)
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
        if (action != null && action == NotificationActionsHelper.REVIEW_TAKEN) {
            val title = context.getString(R.string.received_review_title)
            val justText: String = textResolver.fromHtml(htmlText).toString()

            val intent = getReviewIntent(notification = stepikNotification)
            if (intent == null) {
                analytic.reportEvent(Analytic.Notification.CANT_PARSE_NOTIFICATION, stepikNotification.id.toString())
                return
            }

            val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
            taskBuilder.addParentStack(LessonActivity::class.java)
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
        if (action != null && (action == NotificationActionsHelper.REPLIED || action == NotificationActionsHelper.COMMENTED)) {
            val title = context.getString(R.string.new_message_title)
            val justText: String = textResolver.fromHtml(htmlText).toString()

            val intent = getCommentIntent(stepikNotification)
            if (intent == null) {
                analytic.reportEvent(Analytic.Notification.CANT_PARSE_NOTIFICATION, id.toString())
                return
            }

            val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
            taskBuilder.addParentStack(LessonActivity::class.java)
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
        if (action != null && action == NotificationActionsHelper.ISSUED_CERTIFICATE) {
            val title = context.getString(R.string.get_certifcate_title)
            val justText: String = textResolver.fromHtml(rawMessageHtml).toString()

            val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
            taskBuilder.addParentStack(LessonActivity::class.java)
            taskBuilder.addNextIntent(prepareNotificationIntent(screenManager.certificateIntent, id))
            analytic.reportEventWithIdName(Analytic.Notification.NOTIFICATION_SHOWN, id.toString(), stepikNotification.type?.name)
            val notification = notificationHelper.makeSimpleNotificationBuilder(stepikNotification, justText, taskBuilder, title, id = id)
            stepikNotificationManager.showNotification(id, notification.build())
        } else if (action == NotificationActionsHelper.ISSUED_LICENSE) {
            val title = context.getString(R.string.get_license_message)
            val justText: String = textResolver.fromHtml(rawMessageHtml).toString()

            val intent = getLicenseIntent(notification = stepikNotification) ?: return

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
        if (notification.action != null && notification.action == NotificationActionsHelper.ISSUED_CERTIFICATE) {
            analytic.reportEvent(Analytic.Certificate.OPEN_CERTIFICATE_FROM_NOTIFICATION_CENTER)
            screenManager.showCertificates(context)
            return true
        } else if (notification.action == NotificationActionsHelper.ISSUED_LICENSE) {
            val intent: Intent = getLicenseIntent(notification) ?: return false
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
        val intent: Intent = getCommentIntent(notification) ?: return false
        analytic.reportEvent(Analytic.Notification.OPEN_COMMENT_NOTIFICATION_LINK)
        context.startActivity(intent)
        return true
    }

    private fun openReviewNotification(notification: Notification): Boolean {
        val intent = getReviewIntent(notification) ?: return false
        context.startActivity(intent)
        analytic.reportEvent(Analytic.Notification.OPEN_LESSON_NOTIFICATION_LINK)
        return true
    }

    private fun openTeach(notification: Notification): Boolean {
        val intent: Intent? = getTeachIntent(notification) ?: return false
        analytic.reportEvent(Analytic.Notification.OPEN_TEACH_CENTER)
        context.startActivity(intent)
        return true
    }

    private fun openDefault(notification: Notification): Boolean {
        if (notification.action != null && notification.action == NotificationActionsHelper.ADDED_TO_GROUP) {
            val intent = getDefaultIntent(notification) ?: return false
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

    private fun getTeachIntent(notification: Notification): Intent? {
        val link = HtmlHelper.parseNLinkInText(notification.htmlText ?: "", configs.baseUrl, 0) ?: return null
        try {
            val url = Uri.parse(link)
            val intent: Intent =
                when (url.pathSegments[0]) {
                    "course" ->
                        Intent(context, CourseActivity::class.java)
                    "lesson" ->
                        Intent(context, LessonActivity::class.java)
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

    private fun getLicenseIntent(notification: Notification): Intent? {
        val link = HtmlHelper.parseNLinkInText(notification.htmlText ?: "", configs.baseUrl, 0) ?: return null
        val intent = screenManager.getOpenInWebIntent(link)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    private fun getDefaultIntent(notification: Notification): Intent? {
        val data = HtmlHelper.parseNLinkInText(notification.htmlText ?: "", configs.baseUrl, 1) ?: return null
        val intent = Intent(context, CourseActivity::class.java)
        intent.data = Uri.parse(data)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    private fun getReviewIntent(notification: Notification): Intent? {
        val data = HtmlHelper.parseNLinkInText(notification.htmlText ?: "", configs.baseUrl, 0) ?: return null
        val intent = Intent(context, LessonActivity::class.java)
        intent.data = Uri.parse(data)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    private fun getCommentIntent(notification: Notification): Intent? {
        val action = notification.action
        val htmlText = notification.htmlText ?: ""
        val link =
            if (action == NotificationActionsHelper.REPLIED) {
                HtmlHelper.parseNLinkInText(htmlText, configs.baseUrl, 1) ?: return null
            } else {
                HtmlHelper.parseNLinkInText(htmlText, configs.baseUrl, 3) ?: return null
            }
        val intent = Intent(context, LessonActivity::class.java)
        intent.data = Uri.parse(link)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent
    }

    private fun getCourse(courseId: Long?): Course? {
        if (courseId == null) return null
        var course: Course? = databaseFacade.getCourseById(courseId)
        if (course == null) {
            course = courseRepository.getCourses(courseId, primarySourceType = DataSourceType.REMOTE).blockingGet().firstOrNull()
        }
        return course
    }
}