package org.stepik.android.domain.course_payments.repository

import io.reactivex.Single
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import org.stepik.android.domain.course_payments.model.CoursePayment

interface CoursePaymentsRepository {
    fun createCoursePayment(courseId: Long, sku: Sku, purchase: Purchase): Single<CoursePayment>

    fun getCoursePaymentsByCourseIds(vararg courseIds: Long): Single<List<CoursePayment>>
}