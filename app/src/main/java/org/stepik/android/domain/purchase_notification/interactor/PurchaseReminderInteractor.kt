package org.stepik.android.domain.purchase_notification.interactor

import io.reactivex.Completable
import org.stepic.droid.analytic.experiments.CoursePurchaseReminderSplitTest
import org.stepic.droid.util.AppConstants
import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.data.purchase_notification.model.PurchaseNotificationScheduled
import org.stepik.android.domain.purchase_notification.repository.PurchaseNotificationRepository
import org.stepik.android.view.purchase_notification.notification.PurchaseNotificationDelegate
import java.util.Calendar
import javax.inject.Inject

class PurchaseReminderInteractor
@Inject
constructor(
    private val purchaseNotificationDelegate: PurchaseNotificationDelegate,
    private val coursePurchaseReminderSplitTest: CoursePurchaseReminderSplitTest,
    private val purchaseNotificationRepository: PurchaseNotificationRepository
) {
    companion object {
        private const val MILLIS_IN_1_HOUR = 3600000L
    }
    fun savePurchaseNotificationSchedule(courseId: Long): Completable =
        purchaseNotificationRepository
            .getClosestTimeStamp()
            .flatMapCompletable { timeStamp ->
                val baseTime = if (timeStamp > DateTimeHelper.nowUtc()) {
                    timeStamp
                } else {
                    DateTimeHelper.nowUtc()
                }
                purchaseNotificationRepository.savePurchaseNotificationSchedule(
                    PurchaseNotificationScheduled(courseId, calculateScheduleOffset(baseTime))
                )
            }
            .doOnComplete { purchaseNotificationDelegate.schedulePurchaseNotification() }

    private fun calculateScheduleOffset(timeStamp: Long): Long {
        val scheduledTime = timeStamp + (coursePurchaseReminderSplitTest.currentGroup.notificationDelayHours * MILLIS_IN_1_HOUR)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = scheduledTime

        val scheduledHour = calendar.get(Calendar.HOUR_OF_DAY)
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val nowAt12 = calendar.timeInMillis

        return when {
            scheduledHour < 12 ->
                nowAt12
            scheduledHour >= 21 ->
                nowAt12 + AppConstants.MILLIS_IN_24HOURS
            else ->
                scheduledTime
        }
    }
}