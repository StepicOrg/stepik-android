package org.stepik.android.data.wishlist.source

import io.reactivex.Single
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.wishlist.model.WishlistWrapper

interface WishlistRemoteDataSource {
    fun getWishlistRecord(): Single<StorageRecord<WishlistWrapper>>
    fun createWishlistRecord(wishlistWrapper: WishlistWrapper): Single<StorageRecord<WishlistWrapper>>
    fun updateWishlistRecord(record: StorageRecord<WishlistWrapper>): Single<StorageRecord<WishlistWrapper>>
}