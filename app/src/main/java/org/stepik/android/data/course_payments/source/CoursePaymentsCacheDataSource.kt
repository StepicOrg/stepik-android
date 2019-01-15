package org.stepik.android.data.course_payments.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_payments.model.CoursePayment

interface CoursePaymentsCacheDataSource {
    fun saveCoursePayment(coursePayment: CoursePayment): Completable =
        saveCoursePayments(listOf(coursePayment))

    fun saveCoursePayments(coursePayments: List<CoursePayment>): Completable
    
    fun getCoursePaymentsByCourseIds(vararg courseIds: Long): Single<List<CoursePayment>>
}