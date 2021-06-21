package org.stepik.android.remote.wishlist.mapper

import com.google.gson.Gson
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepic.droid.web.storage.model.StorageRecordWrapped
import org.stepik.android.data.wishlist.KIND_WISHLIST
import org.stepik.android.remote.wishlist.model.WishlistWrapper
import org.stepik.android.remote.remote_storage.model.StorageRequest
import org.stepik.android.remote.remote_storage.model.StorageResponse
import javax.inject.Inject

class WishlistMapper
@Inject
constructor(
    private val gson: Gson
) {
    fun mapToStorageRequest(wishlistWrapper: WishlistWrapper, recordId: Long? = null): StorageRequest =
        StorageRequest(
                StorageRecordWrapped(
                    id = recordId,
                    kind = KIND_WISHLIST,
                    data = gson.toJsonTree(wishlistWrapper)
                )
        )

    fun mapToStorageRequest(record: StorageRecord<WishlistWrapper>): StorageRequest =
        StorageRequest(record.wrap(gson))

    fun mapToStorageRecord(response: StorageResponse): StorageRecord<WishlistWrapper>? =
        response
            .records
            .firstOrNull()
            ?.unwrap(gson)
}