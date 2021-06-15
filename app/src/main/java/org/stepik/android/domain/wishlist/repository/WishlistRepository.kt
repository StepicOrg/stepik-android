package org.stepik.android.domain.wishlist.repository

import io.reactivex.Single
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.wishlist.model.WishlistWrapper

interface WishlistRepository {
    fun updateWishlistRecord(record: StorageRecord<WishlistWrapper>): Single<StorageRecord<WishlistWrapper>>
    fun getWishlistRecord(sourceType: DataSourceType, allowFallback: Boolean = false): Single<StorageRecord<WishlistWrapper>>
}