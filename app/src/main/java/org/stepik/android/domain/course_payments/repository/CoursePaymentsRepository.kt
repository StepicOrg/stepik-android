package org.stepik.android.domain.course_payments.repository

import io.reactivex.Single
import org.stepik.android.domain.course_payments.model.CoursePayment

interface CoursePaymentsRepository {
//    fun createCoursePayment(courseId: Long, sku: Sku, purchase: Purchase): Single<CoursePayment>

    /**
     * Return course payments for selected course id
     *
     * @param coursePaymentStatus - course payments status filter, if null no filter will be applied
     */
    fun getCoursePaymentsByCourseId(courseId: Long, coursePaymentStatus: CoursePayment.Status? = null): Single<List<CoursePayment>>
}