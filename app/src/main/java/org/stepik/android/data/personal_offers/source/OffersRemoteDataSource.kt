package org.stepik.android.data.personal_offers.source

import io.reactivex.Maybe
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_offers.model.OffersWrapper

interface OffersRemoteDataSource {
    fun getOffersRecords(): Maybe<StorageRecord<OffersWrapper>>
}