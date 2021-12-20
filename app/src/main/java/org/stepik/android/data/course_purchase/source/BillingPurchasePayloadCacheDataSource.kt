package org.stepik.android.data.course_purchase.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_purchase.model.BillingPurchasePayload

interface BillingPurchasePayloadCacheDataSource {
    fun getPurchaseOrderPayload(orderId: String): Single<BillingPurchasePayload>
    fun savePurchaseOrderPayload(billingPurchasePayload: BillingPurchasePayload): Completable
}