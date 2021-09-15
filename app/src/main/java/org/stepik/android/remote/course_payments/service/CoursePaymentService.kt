package org.stepik.android.remote.course_payments.service

import io.reactivex.Single
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.remote.course_payments.model.CoursePaymentRequest
import org.stepik.android.remote.course_payments.model.CoursePaymentsResponse
import org.stepik.android.remote.course_payments.model.PromoCodeRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CoursePaymentService {
    @POST("api/course-payments")
    fun createCoursePayment(
        @Body coursePaymentRequest: CoursePaymentRequest
    ): Single<CoursePaymentsResponse>

    @GET("api/course-payments?order=-id")
    fun getCoursePaymentsByCourseId(
        @Query("course") course: Long
    ): Single<CoursePaymentsResponse>

    @POST("api/promo-codes/check")
    fun checkDeeplinkPromoCodeValidity(
        @Body promoCodeRequest: PromoCodeRequest
    ): Single<DeeplinkPromoCode>
}