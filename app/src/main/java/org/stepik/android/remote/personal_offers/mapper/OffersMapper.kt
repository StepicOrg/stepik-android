package org.stepik.android.remote.personal_offers.mapper

import com.google.gson.Gson
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepic.droid.web.storage.model.StorageRecordWrapped
import org.stepik.android.data.personal_offers.getKindOfRecord
import org.stepik.android.domain.personal_offers.model.OffersWrapper
import org.stepik.android.remote.remote_storage.model.StorageRequest
import org.stepik.android.remote.remote_storage.model.StorageResponse
import javax.inject.Inject

class OffersMapper
@Inject
constructor(
    private val gson: Gson
) {
    fun mapToStorageRequest(offers: OffersWrapper, recordId: Long? = null): StorageRequest =
        StorageRequest(
            StorageRecordWrapped(
                id = recordId,
                kind = getKindOfRecord(),
                data = gson.toJsonTree(offers)
            )
        )

    fun mapToStorageRecord(response: StorageResponse): StorageRecord<OffersWrapper>? =
        response
            .records
            .firstOrNull()
            ?.let(::unwrapStorageRecord)

    fun mapToStorageRecordList(response: StorageResponse): List<StorageRecord<OffersWrapper>> =
        response
            .records
            .mapNotNull(::unwrapStorageRecord)

    private fun unwrapStorageRecord(record: StorageRecordWrapped): StorageRecord<OffersWrapper>? =
        record
            .unwrap<OffersWrapper>(gson)
            .takeIf { it.data.promoStories != null }
}