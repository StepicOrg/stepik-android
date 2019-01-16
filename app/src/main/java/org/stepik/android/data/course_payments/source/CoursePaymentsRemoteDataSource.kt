package org.stepik.android.data.course_payments.source

import io.reactivex.Single
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import org.stepik.android.domain.course_payments.model.CoursePayment

interface CoursePaymentsRemoteDataSource {

    fun createCoursePayment(courseId: Long, sku: Sku, purchase: Purchase): Single<CoursePayment>

    fun getCoursePaymentsByCourseId(courseId: Long): Single<List<CoursePayment>>

}