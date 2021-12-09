package org.stepik.android.domain.course_payments.repository

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode

interface CoursePaymentsRepository {
    fun createCoursePayment(courseId: Long, sku: SkuDetails, purchase: Purchase, promoCode: String?): Single<CoursePayment>

    /**
     * Return course payments for selected course id
     *
     * @param coursePaymentStatus - course payments status filter, if null no filter will be applied
     */
    fun getCoursePaymentsByCourseId(courseId: Long, coursePaymentStatus: CoursePayment.Status? = null, sourceType: DataSourceType = DataSourceType.CACHE): Single<List<CoursePayment>>

    fun checkDeeplinkPromoCodeValidity(courseId: Long, name: String): Single<DeeplinkPromoCode>
}