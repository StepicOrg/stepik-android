package org.stepik.android.cache.course_purchase

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.cache.course_purchase.dao.BillingPurchasePayloadDao
import org.stepik.android.data.course_purchase.source.BillingPurchasePayloadCacheDataSource
import org.stepik.android.domain.course_purchase.model.BillingPurchasePayload
import javax.inject.Inject

class BillingPurchasePayloadCacheDataSourceImpl
@Inject
constructor(
    private val billingPurchasePayloadDao: BillingPurchasePayloadDao
) : BillingPurchasePayloadCacheDataSource {
    override fun getPurchaseOrderPayload(orderId: String): Single<BillingPurchasePayload> =
        billingPurchasePayloadDao.getBillingPurchasePayload(orderId)

    override fun savePurchaseOrderPayload(billingPurchasePayload: BillingPurchasePayload): Completable =
        billingPurchasePayloadDao.saveBillingPurchasePayload(billingPurchasePayload)
}