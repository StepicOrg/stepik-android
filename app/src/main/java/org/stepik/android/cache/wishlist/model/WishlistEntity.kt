package org.stepik.android.cache.wishlist.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WishlistEntity(
    @SerializedName("recordId")
    val recordId: Long,
    @SerializedName("courses")
    val courses: List<Long>
) : Parcelable