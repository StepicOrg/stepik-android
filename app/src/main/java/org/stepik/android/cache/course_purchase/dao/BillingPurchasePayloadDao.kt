package org.stepik.android.cache.course_purchase.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_purchase.model.BillingPurchasePayload

@Dao
interface BillingPurchasePayloadDao {
    @Query("SELECT * FROM BillingPurchasePayload WHERE orderId = :orderId")
    fun getBillingPurchasePayload(orderId: String): Single<BillingPurchasePayload>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveBillingPurchasePayload(billingPurchasePayload: BillingPurchasePayload): Completable

    @Query("DELETE FROM BillingPurchasePayload WHERE orderId = :orderId")
    fun deleteBillingPurchasePayload(orderId: String): Completable
}