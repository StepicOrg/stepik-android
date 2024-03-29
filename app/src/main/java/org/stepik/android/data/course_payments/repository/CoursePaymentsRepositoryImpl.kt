package org.stepik.android.data.course_payments.repository

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import io.reactivex.Single
import org.stepik.android.data.course_payments.source.CoursePaymentsCacheDataSource
import org.stepik.android.data.course_payments.source.CoursePaymentsRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import ru.nobird.android.domain.rx.doCompletableOnSuccess
import javax.inject.Inject

class CoursePaymentsRepositoryImpl
@Inject
constructor(
    private val coursePaymentsRemoteDataSource: CoursePaymentsRemoteDataSource,
    private val coursePaymentsCacheDataSource: CoursePaymentsCacheDataSource
) : CoursePaymentsRepository {
    override fun createCoursePayment(courseId: Long, sku: SkuDetails, purchase: Purchase, promoCode: String?): Single<CoursePayment> =
        coursePaymentsRemoteDataSource
            .createCoursePayment(courseId, sku, purchase, promoCode)

    override fun getCoursePaymentsByCourseId(courseId: Long, coursePaymentStatus: CoursePayment.Status?, sourceType: DataSourceType): Single<List<CoursePayment>> =
        when (sourceType) {
            DataSourceType.REMOTE ->
                coursePaymentsRemoteDataSource
                    .getCoursePaymentsByCourseId(courseId, coursePaymentStatus)
                    .doCompletableOnSuccess(coursePaymentsCacheDataSource::saveCoursePayments)

            DataSourceType.CACHE ->
                coursePaymentsCacheDataSource.getCoursePaymentsByCourseId(courseId, coursePaymentStatus)

            else ->
                throw IllegalArgumentException("Unsupported source type = $sourceType")
        }

    override fun checkDeeplinkPromoCodeValidity(courseId: Long, name: String): Single<DeeplinkPromoCode> =
        coursePaymentsRemoteDataSource
            .checkDeeplinkPromoCodeValidity(courseId, name)
            .onErrorReturnItem(DeeplinkPromoCode.EMPTY)
}