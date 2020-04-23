package org.stepik.android.view.personal_deadlines.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.notifications.model.StepikNotificationChannel
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.cache.personal_deadlines.model.DeadlineEntity
import org.stepik.android.domain.personal_deadlines.interactor.DeadlinesNotificationInteractor
import org.stepik.android.view.course.ui.activity.CourseActivity
import org.stepik.android.view.notification.NotificationDelegate
import org.stepik.android.view.notification.StepikNotificationManager
import org.stepik.android.view.notification.helpers.NotificationHelper
import javax.inject.Inject

class DeadlinesNotificationDelegate
@Inject
constructor(
    private val context: Context,
    private val deadlinesNotificationInteractor: DeadlinesNotificationInteractor,
    private val notificationHelper: NotificationHelper,
    stepikNotificationManager: StepikNotificationManager
) : NotificationDelegate("show_deadlines_notification", stepikNotificationManager) {

    companion object {
        private const val OFFSET_12HOURS = 12 * AppConstants.MILLIS_IN_1HOUR
        private const val OFFSET_36HOURS = 36 * AppConstants.MILLIS_IN_1HOUR
    }

    override fun onNeedShowNotification() {
        val now = DateTimeHelper.nowUtc()
        deadlinesNotificationInteractor
            .getDeadlineRecordsForTimestamp(now)
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
        val timestamp = deadlinesNotificationInteractor.getClosestDeadlineTimestamp()
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
        if (timestamp > 0) {
            scheduleNotificationAt(timestamp)
        }
    }

    private fun showPersonalDeadlineNotification(deadline: DeadlineEntity) {
        val course = deadlinesNotificationInteractor.getCourse(deadline.courseId)
        val section = deadlinesNotificationInteractor.getSection(deadline.sectionId)

        if (course == null || section == null) return

        val largeIcon = notificationHelper.getPictureByCourse(course)
        val colorArgb = context.resolveColorAttribute(R.attr.colorSecondary)

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
}