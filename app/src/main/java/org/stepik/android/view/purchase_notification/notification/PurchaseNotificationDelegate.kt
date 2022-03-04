package org.stepik.android.view.purchase_notification.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.notifications.model.StepikNotificationChannel
import org.stepic.droid.util.resolveColorAttribute
import org.stepik.android.data.purchase_notification.model.PurchaseNotificationScheduled
import org.stepik.android.domain.base.analytic.BundleableAnalyticEvent
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.purchase_notification.analytic.PurchaseNotificationClicked
import org.stepik.android.domain.purchase_notification.analytic.PurchaseNotificationDismissed
import org.stepik.android.domain.purchase_notification.analytic.PurchaseNotificationShown
import org.stepik.android.domain.purchase_notification.interactor.PurchaseNotificationInteractor
import org.stepik.android.view.base.receiver.DismissedNotificationReceiver
import org.stepik.android.view.course.ui.activity.CourseActivity
import org.stepik.android.view.notification.NotificationDelegate
import org.stepik.android.view.notification.StepikNotificationManager
import org.stepik.android.view.notification.helpers.NotificationHelper
import javax.inject.Inject

class PurchaseNotificationDelegate
@Inject
constructor(
    private val context: Context,
    private val analytic: Analytic,
    private val purchaseNotificationInteractor: PurchaseNotificationInteractor,
    private val notificationHelper: NotificationHelper,
    stepikNotificationManager: StepikNotificationManager
) : NotificationDelegate("purchase_course_notification", stepikNotificationManager) {
    companion object {
        private const val PURCHASE_NOTIFICATION_ID = 5123L
    }

    override fun onNeedShowNotification() {
        val scheduledNotification = purchaseNotificationInteractor.getClosestExpiredScheduledNotification()
        showPurchaseNotification(scheduledNotification)
        schedulePurchaseNotification()
    }

    fun schedulePurchaseNotification() {
        val timeStamp = purchaseNotificationInteractor.getClosestScheduledTimestamp()
        if (timeStamp > 0) {
            scheduleNotificationAt(timeStamp)
        }
    }

    private fun showPurchaseNotification(purchaseNotificationScheduled: PurchaseNotificationScheduled?) {
        if (purchaseNotificationScheduled == null) return

        val course = purchaseNotificationInteractor
            .getCourse(purchaseNotificationScheduled.courseId)
            ?: return

        if (purchaseNotificationInteractor.isHasCoursePayments(courseId = course.id)) return

        val intent = CourseActivity.createIntent(
            context,
            course,
            CourseViewSource.PurchaseReminderNotification
        )
        intent.putExtra(BundleableAnalyticEvent.BUNDLEABLE_ANALYTIC_EVENT, PurchaseNotificationClicked(course.id).toBundle())
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val taskBuilder: TaskStackBuilder = TaskStackBuilder.create(context)
        taskBuilder.addParentStack(CourseActivity::class.java)
        taskBuilder.addNextIntent(intent)

        val deleteIntent = DismissedNotificationReceiver.createIntent(context, PurchaseNotificationDismissed(course.id).toBundle())
        val deletePendingIntent = PendingIntent.getBroadcast(context, DismissedNotificationReceiver.REQUEST_CODE, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val largeIcon = notificationHelper.getPictureByCourse(course)
        val colorArgb = context.resolveColorAttribute(R.attr.colorSecondary)
        val title = context.getString(R.string.purchase_notification_title)
        val message = context.getString(
            R.string.purchase_notification_description,
            course.title
        )

        val pendingIntent = taskBuilder.getPendingIntent(course.id.toInt(), PendingIntent.FLAG_ONE_SHOT)
        val notification = NotificationCompat.Builder(context, StepikNotificationChannel.user.channelId)
            .setLargeIcon(largeIcon)
            .setSmallIcon(R.drawable.ic_notification_icon_1)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(colorArgb)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDeleteIntent(deletePendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        analytic.report(PurchaseNotificationShown(course.id))
        showNotification(PURCHASE_NOTIFICATION_ID, notification.build())
    }
}