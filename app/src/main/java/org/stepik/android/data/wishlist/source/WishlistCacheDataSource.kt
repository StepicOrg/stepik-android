package org.stepik.android.data.wishlist.source

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepik.android.domain.wishlist.model.WishlistEntity

interface WishlistCacheDataSource {
    fun getWishlistRecord(): Maybe<WishlistEntity>
    fun saveWishlistRecord(wishlistEntity: WishlistEntity): Completable
}