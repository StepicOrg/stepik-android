package org.stepik.android.data.course_payments.repository

import io.reactivex.Single
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepik.android.data.course_payments.source.CoursePaymentsCacheDataSource
import org.stepik.android.data.course_payments.source.CoursePaymentsRemoteDataSource
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import javax.inject.Inject

class CoursePaymentsRepositoryImpl
@Inject
constructor(
    private val coursePaymentsRemoteDataSource: CoursePaymentsRemoteDataSource,
    private val coursePaymentsCacheDataSource: CoursePaymentsCacheDataSource
) : CoursePaymentsRepository {
    override fun createCoursePayment(courseId: Long, sku: Sku, purchase: Purchase): Single<CoursePayment> =
        coursePaymentsRemoteDataSource
            .createCoursePayment(courseId, sku, purchase)
            .doCompletableOnSuccess(coursePaymentsCacheDataSource::saveCoursePayment)

    override fun getCoursePaymentsByCourseIds(vararg courseIds: Long): Single<List<CoursePayment>> =
        coursePaymentsRemoteDataSource
            .getCoursePaymentsByCourseIds(*courseIds) // todo resolve caching
            .doCompletableOnSuccess(coursePaymentsCacheDataSource::saveCoursePayments)

}