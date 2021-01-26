package org.stepik.android.domain.personal_offers.repository

import io.reactivex.Single
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.domain.personal_offers.model.OffersWrapper

interface OffersRepository {
    fun getOffersRecord(): Single<StorageRecord<OffersWrapper>>
}