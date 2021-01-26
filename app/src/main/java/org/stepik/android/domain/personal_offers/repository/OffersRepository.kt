package org.stepik.android.domain.personal_offers.repository

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_offers.model.OffersWrapper

interface OffersRepository {
    fun getOffersRecord(): Maybe<StorageRecord<OffersWrapper>>
    fun createOffersRecord(): Single<StorageRecord<OffersWrapper>>
}