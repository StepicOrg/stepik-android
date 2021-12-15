package org.stepik.android.domain.course.repository

import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseDataResult

interface CoursePurchaseDataRepository {
    fun getDeeplinkPromoCode(): DeeplinkPromoCode
    fun getCoursePurchaseData(): CoursePurchaseDataResult
    fun savePurchaseData(deeplinkPromoCode: DeeplinkPromoCode, coursePurchaseDataResult: CoursePurchaseDataResult)
}