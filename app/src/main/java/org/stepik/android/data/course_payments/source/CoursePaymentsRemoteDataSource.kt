package org.stepik.android.data.course_payments.source

import io.reactivex.Completable
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku

interface CoursePaymentsRemoteDataSource {

    fun createCoursePayment(courseId: Long, sku: Sku, purchase: Purchase): Completable

}