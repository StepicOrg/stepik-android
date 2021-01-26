package org.stepik.android.data.personal_offers.source

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_offers.model.OffersWrapper

interface OffersRemoteDataSource {
    fun getOffersRecord(): Maybe<StorageRecord<OffersWrapper>>
    fun createOffersRecord(): Single<StorageRecord<OffersWrapper>>
}