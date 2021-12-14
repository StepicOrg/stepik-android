package org.stepik.android.domain.course.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseDataResult

interface CoursePurchaseDataRepository {
    fun getDeeplinkPromoCode(): BehaviorRelay<DeeplinkPromoCode>
    fun getCoursePurchaseData(): BehaviorRelay<CoursePurchaseDataResult>
    fun saveDeeplinkPromoCode(deeplinkPromoCode: DeeplinkPromoCode): Completable
    fun saveCoursePurchaseData(coursePurchaseData: CoursePurchaseDataResult): Completable
}