package org.stepik.android.domain.mobile_tiers.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LightSku(
    @PrimaryKey
    val id: String,
    val price: String
)
