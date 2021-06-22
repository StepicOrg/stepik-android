package org.stepik.android.data.wishlist.source

import io.reactivex.Single
import org.stepik.android.domain.wishlist.model.WishlistEntity
import org.stepik.android.remote.wishlist.model.WishlistWrapper

interface WishlistRemoteDataSource {
    fun getWishlistRecord(): Single<WishlistEntity>
    fun createWishlistRecord(wishlistWrapper: WishlistWrapper): Single<WishlistEntity>
    fun updateWishlistRecord(wishlistEntity: WishlistEntity): Single<WishlistEntity>
}