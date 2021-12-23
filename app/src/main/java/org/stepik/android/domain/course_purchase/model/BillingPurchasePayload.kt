package org.stepik.android.domain.course_purchase.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BillingPurchasePayload(
    @PrimaryKey
    val orderId: String,
    val courseId: Long,
    val profileId: Long,
    val obfuscatedAccountId: String,
    val obfuscatedProfileId: String,
    val promoCode: String?
) {
    companion object {
        val EMPTY = BillingPurchasePayload("", 0L, 0L, "", "", null)
    }
}