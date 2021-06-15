package org.stepik.android.cache.wishlist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WishlistEntity(
    @PrimaryKey
    val recordId: Long,
    val courses: List<Long>
)
