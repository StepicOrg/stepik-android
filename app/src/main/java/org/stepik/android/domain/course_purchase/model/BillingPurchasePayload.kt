package org.stepik.android.domain.course_purchase.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BillingPurchasePayload(
    @PrimaryKey
    val orderId: String,
    val obfuscatedAccountId: String,
    val obfuscatedProfileId: String,
    val promoCode: String?
)