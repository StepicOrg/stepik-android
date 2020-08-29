package org.stepik.android.domain.purchase_notification.interactor

import org.stepik.android.data.purchase_notification.model.PurchaseNotificationScheduled
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import org.stepik.android.domain.purchase_notification.repository.PurchaseNotificationRepository
import org.stepik.android.model.Course
import javax.inject.Inject

class PurchaseNotificationInteractor
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val coursePaymentsRepository: CoursePaymentsRepository,
    private val purchaseNotificationRepository: PurchaseNotificationRepository
) {
    fun getCourse(courseId: Long): Course? =
        courseRepository.getCourse(courseId).blockingGet()

    fun isHasCoursePayments(courseId: Long): Boolean =
        coursePaymentsRepository
            .getCoursePaymentsByCourseId(courseId, coursePaymentStatus = CoursePayment.Status.SUCCESS, primarySourceType = DataSourceType.REMOTE)
            .onErrorReturnItem(emptyList())
            .blockingGet()
            .isNotEmpty()

    fun getClosestScheduledTimestamp(): Long =
        purchaseNotificationRepository
            .getClosestTimeStamp()
            .onErrorReturnItem(0L)
            .blockingGet()

    fun getClosestExpiredScheduledNotification(): PurchaseNotificationScheduled? =
        purchaseNotificationRepository
            .getClosestExpiredScheduledNotification()
            .blockingGet()
}