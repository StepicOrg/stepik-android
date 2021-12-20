package org.stepik.android.data.course_purchase.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.data.course_purchase.source.BillingPurchasePayloadCacheDataSource
import org.stepik.android.domain.course_purchase.model.BillingPurchasePayload
import org.stepik.android.domain.course_purchase.repository.BillingPurchasePayloadRepository
import javax.inject.Inject

class BillingPurchasePayloadRepositoryImpl
@Inject
constructor(
    private val billingPurchasePayloadCacheDataSource: BillingPurchasePayloadCacheDataSource
) : BillingPurchasePayloadRepository {
    override fun getBillingPurchasePayload(orderId: String): Single<BillingPurchasePayload> =
        billingPurchasePayloadCacheDataSource.getPurchaseOrderPayload(orderId)

    override fun saveBillingPurchasePayload(billingPurchasePayload: BillingPurchasePayload): Completable =
        billingPurchasePayloadCacheDataSource.savePurchaseOrderPayload(billingPurchasePayload)

    override fun deleteBillingPurchasePayload(orderId: String): Completable =
        billingPurchasePayloadCacheDataSource.deletePurchaseOrderPayload(orderId)
}