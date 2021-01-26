package org.stepik.android.remote.personal_offers.mapper

import com.google.gson.Gson
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_offers.model.OffersWrapper
import org.stepik.android.remote.remote_storage.model.StorageResponse
import javax.inject.Inject

class OffersMapper
@Inject
constructor(
    private val gson: Gson
) {
    fun mapToStorageRecord(response: StorageResponse): StorageRecord<OffersWrapper>? =
        response
            .records
            .firstOrNull()
            ?.unwrap<OffersWrapper>(gson)
}