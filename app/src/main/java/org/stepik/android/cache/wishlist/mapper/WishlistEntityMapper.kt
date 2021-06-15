package org.stepik.android.cache.wishlist.mapper

import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.cache.wishlist.model.WishlistEntity
import org.stepik.android.domain.wishlist.model.WishlistWrapper
import javax.inject.Inject

class WishlistEntityMapper
@Inject
constructor() {
    fun mapToEntity(record: StorageRecord<WishlistWrapper>): WishlistEntity =
        WishlistEntity(
            recordId = record.id ?: -1,
            courses = record.data.courses ?: emptyList()
        )
}