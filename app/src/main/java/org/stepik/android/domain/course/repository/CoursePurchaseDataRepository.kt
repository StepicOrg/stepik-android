package org.stepik.android.domain.course.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData

interface CoursePurchaseDataRepository {
    fun getDeeplinkPromoCode(): Single<DeeplinkPromoCode>
    fun getCoursePurchaseData(): Maybe<CoursePurchaseData>
    fun saveDeeplinkPromoCode(deeplinkPromoCode: DeeplinkPromoCode): Completable
    fun saveCoursePurchaseData(coursePurchaseData: CoursePurchaseData): Completable
}