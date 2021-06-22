package org.stepik.android.domain.wishlist.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WishlistEntity(
    @SerializedName("recordId")
    val recordId: Long,
    @SerializedName("courses")
    val courses: List<Long>
) : Parcelable {
    companion object {
        val EMPTY = WishlistEntity(-1, emptyList())
    }
}
