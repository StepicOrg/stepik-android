package org.stepik.android.data.course.repository

import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import javax.inject.Inject

class CoursePurchaseDataRepositoryImpl
@Inject
constructor() {
    var deeplinkPromoCode: DeeplinkPromoCode = DeeplinkPromoCode.EMPTY
    var coursePurchaseData: CoursePurchaseData? = null
}