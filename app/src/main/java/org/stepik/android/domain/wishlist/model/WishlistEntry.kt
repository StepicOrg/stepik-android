package org.stepik.android.domain.wishlist.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.Date

@Entity
data class WishlistEntry(
    @PrimaryKey
    @SerializedName("id")
    val id: Long,
    @SerializedName("course")
    val course: Long,
    @SerializedName("user")
    val user: Long,
    @SerializedName("create_date")
    val createDate: Date,
    @SerializedName("platform")
    val platform: String
)
