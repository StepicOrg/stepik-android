package org.stepik.android.domain.course_purchase.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_purchase.model.BillingPurchasePayload

interface BillingPurchasePayloadRepository {
    fun getBillingPurchasePayload(orderId: String): Single<BillingPurchasePayload>
    fun saveBillingPurchasePayload(billingPurchasePayload: BillingPurchasePayload): Completable
    fun deleteBillingPurchasePayload(orderId: String): Completable
}