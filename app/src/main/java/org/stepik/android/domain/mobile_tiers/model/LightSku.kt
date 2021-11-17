package org.stepik.android.domain.mobile_tiers.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class LightSku(
    @PrimaryKey
    val id: String,
    val price: String
) : Parcelable
