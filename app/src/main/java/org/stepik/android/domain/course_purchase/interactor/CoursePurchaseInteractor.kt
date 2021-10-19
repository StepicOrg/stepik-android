package org.stepik.android.domain.course_purchase.interactor

import io.reactivex.Single
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.domain.course_payments.repository.CoursePaymentsRepository
import javax.inject.Inject

class CoursePurchaseInteractor
@Inject
constructor(
    private val coursePaymentsRepository: CoursePaymentsRepository
) {
    fun checkPromoCodeValidity(courseId: Long, promoCodeName: String): Single<DeeplinkPromoCode> =
        coursePaymentsRepository
            .checkDeeplinkPromoCodeValidity(courseId, promoCodeName)
}