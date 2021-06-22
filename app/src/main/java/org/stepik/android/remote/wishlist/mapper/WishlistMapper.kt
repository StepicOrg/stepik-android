package org.stepik.android.remote.wishlist.mapper

import com.google.gson.Gson
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepic.droid.web.storage.model.StorageRecordWrapped
import org.stepik.android.data.wishlist.KIND_WISHLIST
import org.stepik.android.domain.wishlist.model.WishlistEntity
import org.stepik.android.remote.wishlist.model.WishlistWrapper
import org.stepik.android.remote.remote_storage.model.StorageRequest
import org.stepik.android.remote.remote_storage.model.StorageResponse
import javax.inject.Inject

class WishlistMapper
@Inject
constructor(
    private val gson: Gson
) {
    fun mapToEntity(response: StorageResponse): WishlistEntity? =
        response
            .records
            .firstOrNull()
            ?.unwrap<WishlistWrapper>(gson)
            ?.let(::reverseCoursesInStorageRecord)
            ?.let {
                WishlistEntity(
                    recordId = it.id ?: -1,
                    courses = it.data.courses ?: emptyList()
                )
            }

    fun mapToStorageRequest(wishlistWrapper: WishlistWrapper, recordId: Long? = null): StorageRequest =
        StorageRequest(
                StorageRecordWrapped(
                    id = recordId,
                    kind = KIND_WISHLIST,
                    data = gson.toJsonTree(wishlistWrapper)
                )
        )

    fun mapToStorageRequest(record: StorageRecord<WishlistWrapper>): StorageRequest =
        StorageRequest(record.let(::reverseCoursesInStorageRecord).wrap(gson))

    fun mapToStorageRequest(wishlistEntity: WishlistEntity): StorageRequest =
        StorageRequest(
            StorageRecord(
                id = wishlistEntity.recordId.takeIf { it != -1L },
                kind = KIND_WISHLIST,
                data = WishlistWrapper(wishlistEntity.courses)
            )
            .let(::reverseCoursesInStorageRecord)
            .wrap(gson)
        )

    fun mapToStorageRecord(response: StorageResponse): StorageRecord<WishlistWrapper>? =
        response
            .records
            .firstOrNull()
            ?.unwrap<WishlistWrapper>(gson)
            ?.let(::reverseCoursesInStorageRecord)

    private fun reverseCoursesInStorageRecord(wishlistStorageRecord: StorageRecord<WishlistWrapper>): StorageRecord<WishlistWrapper> =
        wishlistStorageRecord.copy(
            data = wishlistStorageRecord.data.copy(
                courses = wishlistStorageRecord.data.courses?.reversed()
            )
        )
}