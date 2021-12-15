package org.stepik.android.data.course.repository

import org.stepik.android.domain.course.repository.CoursePurchaseDataRepository
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseDataResult
import javax.inject.Inject

class CoursePurchaseDataRepositoryImpl
@Inject
constructor() : CoursePurchaseDataRepository {
    private var deeplinkPromoCode: DeeplinkPromoCode = DeeplinkPromoCode.EMPTY
    private var coursePurchaseDataResult: CoursePurchaseDataResult = CoursePurchaseDataResult.Empty

    @Synchronized
    override fun getDeeplinkPromoCode(): DeeplinkPromoCode =
        deeplinkPromoCode

    @Synchronized
    override fun getCoursePurchaseData(): CoursePurchaseDataResult =
        coursePurchaseDataResult

    override fun savePurchaseData(deeplinkPromoCode: DeeplinkPromoCode, coursePurchaseDataResult: CoursePurchaseDataResult) {
        this.deeplinkPromoCode = deeplinkPromoCode
        this.coursePurchaseDataResult = coursePurchaseDataResult
    }
}