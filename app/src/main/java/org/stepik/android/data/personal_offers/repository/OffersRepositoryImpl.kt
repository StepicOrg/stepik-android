package org.stepik.android.data.personal_offers.repository

import io.reactivex.Single
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.data.personal_offers.source.OffersRemoteDataSource
import org.stepik.android.domain.personal_offers.model.OffersWrapper
import org.stepik.android.domain.personal_offers.repository.OffersRepository
import javax.inject.Inject

class OffersRepositoryImpl
@Inject
constructor(
    private val offersRemoteDataSource: OffersRemoteDataSource
) : OffersRepository {
    override fun getOffersRecord(): Single<StorageRecord<OffersWrapper>> =
        offersRemoteDataSource
            .getOffersRecord()
            .switchIfEmpty(createOffersRecord())

    private fun createOffersRecord(): Single<StorageRecord<OffersWrapper>> =
        offersRemoteDataSource.createOffersRecord()
}