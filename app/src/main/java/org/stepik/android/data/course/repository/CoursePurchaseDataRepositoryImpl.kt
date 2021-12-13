package org.stepik.android.data.course.repository

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.course.repository.CoursePurchaseDataRepository
import org.stepik.android.domain.course_payments.model.DeeplinkPromoCode
import org.stepik.android.presentation.course_purchase.model.CoursePurchaseData
import javax.inject.Inject

class CoursePurchaseDataRepositoryImpl
@Inject
constructor() : CoursePurchaseDataRepository {
    private var deeplinkPromoCode: BehaviorRelay<DeeplinkPromoCode> = BehaviorRelay.createDefault(DeeplinkPromoCode.EMPTY)
    private var coursePurchaseData: BehaviorRelay<CoursePurchaseData> = BehaviorRelay.create()

    override fun getDeeplinkPromoCode(): Single<DeeplinkPromoCode> =
        deeplinkPromoCode.lastElement().toSingle()

    override fun getCoursePurchaseData(): Maybe<CoursePurchaseData> =
        coursePurchaseData.lastElement()

    override fun saveDeeplinkPromoCode(deeplinkPromoCode: DeeplinkPromoCode): Completable =
        Completable.fromAction { this.deeplinkPromoCode.accept(deeplinkPromoCode) }

    override fun saveCoursePurchaseData(coursePurchaseData: CoursePurchaseData): Completable =
        Completable.fromAction { this.coursePurchaseData.accept(coursePurchaseData) }
}