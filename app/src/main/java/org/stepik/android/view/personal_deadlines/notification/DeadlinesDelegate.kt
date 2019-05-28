package org.stepik.android.view.personal_deadlines.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.notifications.model.StepikNotificationChannel
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.ColorUtil
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.web.Api
import org.stepik.android.cache.personal_deadlines.model.DeadlineEntity
import org.stepik.android.data.personal_deadlines.source.DeadlinesCacheDataSource
import org.stepik.android.model.Course
import org.stepik.android.model.Section
import org.stepik.android.view.course.ui.activity.CourseActivity
import org.stepik.android.view.notification.NotificationDelegate
import org.stepik.android.view.notification.StepikNotificationManager
import org.stepik.android.view.notification.helpers.NotificationHelper
import javax.inject.Inject

class DeadlinesDelegate
@Inject constructor(
    private val context: Context,
    private val deadlinesCacheDataSource: DeadlinesCacheDataSource,
    private val api: Api,
    private val databaseFacade: DatabaseFacade,
    private val notificationHelper: NotificationHelper,
    stepikNotificationManager: StepikNotificationManager
) : NotificationDelegate("show_deadlines_notification", stepikNotificationManager) {

    companion object {
        private const val OFFSET_12HOURS = 12 * AppConstants.MILLIS_IN_1HOUR
        private const val OFFSET_36HOURS = 36 * AppConstants.MILLIS_IN_1HOUR
    }

    override fun onNeedShowNotification() {
        val now = DateTimeHelper.nowUtc()
        deadlinesCacheDataSource
            .getDeadlineRecordsForTimestamp(longArrayOf(now + OFFSET_12HOURS, now + OFFSET_36HOURS))
            .map { it.sortedBy(DeadlineEntity::deadline).distinctBy(DeadlineEntity::courseId) }
            .doOnSuccess { deadlines ->
                deadlines.forEach { showPersonalDeadlineNotification(it) }
            }
            .doFinally {
                scheduleDeadlinesNotifications()
            }
            .ignoreElement()
            .onErrorComplete()
            .blockingAwait()
    }

    fun scheduleDeadlinesNotifications() {
        val now = DateTimeHelper.nowUtc()
        val timestamp = deadlinesCacheDataSource
                .getClosestDeadlineTimestamp()
                .onErrorReturnItem(0)
                .blockingGet()
        scheduleDeadlinesNotificationAt(now, timestamp)
    }

    private fun scheduleDeadlinesNotificationAt(now: Long, closestDeadline: Long) {
        val timestamp = when {
            closestDeadline - OFFSET_36HOURS > now ->
                closestDeadline - OFFSET_36HOURS
            closestDeadline - OFFSET_12HOURS > now ->
                closestDeadline - OFFSET_12HOURS
            else -> 0L
        }
        scheduleNotificationAt(timestamp)
    }

    private fun showPersonalDeadlineNotification(deadline: DeadlineEntity) {
        val course = getCourse(deadline.courseId)
        val section = getSection(deadline.sectionId)

        if (course == null || section == null) return

        val largeIcon = notificationHelper.getPictureByCourse(course)
        val colorArgb = ColorUtil.getColorArgb(R.color.stepic_brand_primary)

        val hoursDiff = (deadline.deadline.time - DateTimeHelper.nowUtc()) / AppConstants.MILLIS_IN_1HOUR + 1

        val intent = CourseActivity.createIntent(context, course)
        intent.putExtra(Analytic.Deadlines.Params.BEFORE_DEADLINE, hoursDiff)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        taskBuilder.addParentStack(CourseActivity::class.java)
        taskBuilder.addNextIntent(intent)

        val title = context.getString(R.string.app_name)
        val message = context.getString(
            R.string.deadlines_notification, section.title, course.title,
            context.resources.getQuantityString(R.plurals.hours, hoursDiff.toInt(), hoursDiff))

        val pendingIntent = taskBuilder.getPendingIntent(deadline.sectionId.toInt(), PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(context, StepikNotificationChannel.user.channelId)
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_notification_icon_1)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(colorArgb)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(notificationHelper.getDeleteIntent(deadline.sectionId))
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setNumber(1)

        showNotification(deadline.sectionId, notification.build())
    }

    private fun getCourse(courseId: Long?): Course? {
        if (courseId == null) return null
        var course: Course? = databaseFacade.getCourseById(courseId)
        if (course == null) {
            course = api.getCourse(courseId).execute()?.body()?.courses?.firstOrNull()
        }
        return course
    }

    private fun getSection(sectionId: Long): Section? {
        var section: Section? = databaseFacade.getSectionById(sectionId)
        if (section == null) {
            section = api.getSections(longArrayOf(sectionId)).execute()?.body()?.sections?.firstOrNull()
        }
        return section
    }
}