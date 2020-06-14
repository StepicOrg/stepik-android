package org.stepik.android.remote.course_payments

import io.reactivex.Single
import org.stepik.android.data.course_payments.source.CoursePaymentsRemoteDataSource
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.remote.course_payments.model.CoursePaymentsResponse
import org.stepik.android.remote.course_payments.service.CoursePaymentService
import javax.inject.Inject

class CoursePaymentsRemoteDataSourceImpl
@Inject
constructor(
    private val coursePaymentService: CoursePaymentService
) : CoursePaymentsRemoteDataSource {

//    override fun createCoursePayment(courseId: Long, sku: Sku, purchase: Purchase): Single<CoursePayment> =
//        coursePaymentService
//            .createCoursePayment(
//                CoursePaymentRequest(CoursePaymentRequest.Body(
//                    course   = courseId,
//                    provider = CoursePaymentRequest.Body.Provider.GOOGLE,
//                    data     = CoursePaymentRequest.Body.Data(
//                        token       = purchase.token,
//                        packageName = purchase.packageName,
//                        productId   = purchase.sku,
//                        amount      = sku.detailedPrice.amount / 1_000_000f,
//                        currency    = sku.detailedPrice.currency
//                    )
//                ))
//            )
//            .map { it.coursePayments.first() }

    override fun getCoursePaymentsByCourseId(courseId: Long, coursePaymentStatus: CoursePayment.Status?): Single<List<CoursePayment>> =
        coursePaymentService
            .getCoursePaymentsByCourseId(courseId)
            .map(CoursePaymentsResponse::coursePayments)
            .map { payments ->
                if (coursePaymentStatus != null) {
                    payments.filter { it.status == coursePaymentStatus }
                } else {
                    payments
                }
            }
}