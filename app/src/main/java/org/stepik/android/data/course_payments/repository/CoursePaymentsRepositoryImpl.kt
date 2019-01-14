package org.stepik.android.data.course_payments.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import org.stepik.android.data.course_payments.source.CoursePaymentsRemoteDataSource
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import javax.inject.Inject

class CoursePaymentsRepositoryImpl
@Inject
constructor(
    private val coursePaymentsRemoteDataSource: CoursePaymentsRemoteDataSource
) : CoursePaymentsRepository {
    override fun createCoursePayment(courseId: Long, sku: Sku, purchase: Purchase): Completable =
        coursePaymentsRemoteDataSource.createCoursePayment(courseId, sku, purchase)

    override fun getCoursePaymentsByCourseIds(vararg courseIds: Long): Single<List<CoursePayment>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}