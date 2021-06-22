package org.stepik.android.domain.wishlist.repository

import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.wishlist.model.WishlistEntity

interface WishlistRepository {
    fun updateWishlistRecord(wishlistEntity: WishlistEntity): Single<WishlistEntity>
    fun getWishlistRecord(sourceType: DataSourceType): Single<WishlistEntity>
}