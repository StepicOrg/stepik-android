package org.stepik.android.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ViewRevenue(
    @SerializedName("enabled")
    val enabled: Boolean
) : Parcelable