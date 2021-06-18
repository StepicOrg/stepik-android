package org.stepik.android.cache.wishlist.mapper

import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.cache.wishlist.model.WishlistEntity
import org.stepik.android.data.wishlist.KIND_WISHLIST
import org.stepik.android.domain.wishlist.model.WishlistWrapper
import javax.inject.Inject

// TODO APPS-3355: Either transfer to domain layer or use WishlistEntity in repository
class WishlistEntityMapper
@Inject
constructor() {
    fun mapToEntity(record: StorageRecord<WishlistWrapper>): WishlistEntity =
        WishlistEntity(
            recordId = record.id ?: -1,
            courses = record.data.courses ?: emptyList()
        )

    fun mapToStorageRecord(wishlistEntity: WishlistEntity): StorageRecord<WishlistWrapper> =
        StorageRecord(
            id = wishlistEntity.recordId,
            kind = KIND_WISHLIST,
            data = WishlistWrapper(wishlistEntity.courses)
        )
}