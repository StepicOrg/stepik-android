package org.stepik.android.data.wishlist.source

import io.reactivex.Completable
import io.reactivex.Maybe
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.remote.wishlist.model.WishlistWrapper

interface WishlistCacheDataSource {
    fun getWishlistRecord(): Maybe<StorageRecord<WishlistWrapper>>
    fun saveWishlistRecord(wishlistRecord: StorageRecord<WishlistWrapper>): Completable
}