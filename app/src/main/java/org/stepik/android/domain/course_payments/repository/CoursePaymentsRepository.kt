package org.stepik.android.domain.course_payments.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_payments.model.CoursePayment

interface CoursePaymentsRepository {
    fun createCoursePayment(): Completable

    fun getCoursePaymentsByCourseIds(vararg courseIds: Long): Single<List<CoursePayment>>
}