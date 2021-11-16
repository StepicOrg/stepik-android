package org.stepik.android.domain.mobile_tiers.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class MobileTier(
    @SerializedName("id")
    val id: String,
    @PrimaryKey
    @SerializedName("course")
    val course: Long,
    @SerializedName("price_tier")
    val priceTier: String? = null,
    @SerializedName("promo_tier")
    val promoTier: String? = null
)