package org.stepik.android.remote.course_payments

import io.reactivex.Single
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.Sku
import org.stepic.droid.web.StepicRestLoggedService
import org.stepik.android.data.course_payments.source.CoursePaymentsRemoteDataSource
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.remote.course_payments.model.CoursePaymentRequest
import org.stepik.android.remote.course_payments.model.CoursePaymentsResponse
import javax.inject.Inject

class CoursePaymentsRemoteDataSourceImpl
@Inject
constructor(
    private val loggedService: StepicRestLoggedService
) : CoursePaymentsRemoteDataSource {

    override fun createCoursePayment(courseId: Long, sku: Sku, purchase: Purchase): Single<CoursePayment> =
        loggedService
            .createCoursePayment(
                CoursePaymentRequest(CoursePaymentRequest.Body(
                    course   = courseId,
                    provider = CoursePaymentRequest.Body.Provider.GOOGLE,
                    data     = CoursePaymentRequest.Body.Data(
                        token       = purchase.token,
                        packageName = purchase.packageName,
                        productId   = purchase.sku,
                        amount      = sku.detailedPrice.amount / 1_000_000f,
                        currency    = sku.detailedPrice.currency
                    )
                ))
            )
            .map { it.coursePayments.first() }

    override fun getCoursePaymentsByCourseIds(vararg courseIds: Long): Single<List<CoursePayment>> =
        loggedService
            .getCoursePaymentsByCourseIds(courseIds)
            .map(CoursePaymentsResponse::coursePayments)
}