package org.stepik.android.data.course.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import org.stepik.android.domain.course.repository.CoursePurchaseDataRepository
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseDataResult
import javax.inject.Inject

class CoursePurchaseDataRepositoryImpl
@Inject
constructor() : CoursePurchaseDataRepository {
    private val deeplinkPromoCode: BehaviorRelay<DeeplinkPromoCode> = BehaviorRelay.createDefault(DeeplinkPromoCode.EMPTY)
    private val coursePurchaseData: BehaviorRelay<CoursePurchaseDataResult> = BehaviorRelay.createDefault(CoursePurchaseDataResult.Empty)

    override fun getDeeplinkPromoCode(): BehaviorRelay<DeeplinkPromoCode> =
        deeplinkPromoCode

    override fun getCoursePurchaseData(): BehaviorRelay<CoursePurchaseDataResult> =
        coursePurchaseData

    override fun saveDeeplinkPromoCode(deeplinkPromoCode: DeeplinkPromoCode): Completable =
        Completable.fromAction { this.deeplinkPromoCode.accept(deeplinkPromoCode) }

    override fun saveCoursePurchaseData(coursePurchaseData: CoursePurchaseDataResult): Completable =
        Completable.fromAction { this.coursePurchaseData.accept(coursePurchaseData) }
}