package org.stepik.android.data.course_payments.repository

import io.reactivex.Single
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import org.stepik.android.data.course_payments.source.CoursePaymentsCacheDataSource
import org.stepik.android.data.course_payments.source.CoursePaymentsRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
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

    override fun getCoursePaymentsByCourseId(courseId: Long, coursePaymentStatus: CoursePayment.Status?, primarySourceType: DataSourceType): Single<List<CoursePayment>> =
        when (primarySourceType) {
            DataSourceType.REMOTE ->
                coursePaymentsRemoteDataSource
                    .getCoursePaymentsByCourseId(courseId, coursePaymentStatus)
                    .doCompletableOnSuccess(coursePaymentsCacheDataSource::saveCoursePayments)
                    .onErrorResumeNext { coursePaymentsCacheDataSource.getCoursePaymentsByCourseId(courseId, coursePaymentStatus) }

            DataSourceType.CACHE ->
                coursePaymentsCacheDataSource.getCoursePaymentsByCourseId(courseId, coursePaymentStatus)
        }
}