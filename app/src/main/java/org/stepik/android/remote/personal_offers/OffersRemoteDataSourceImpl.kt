package org.stepik.android.remote.personal_offers

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.data.personal_offers.getKindOfRecord
import org.stepik.android.data.personal_offers.source.OffersRemoteDataSource
import org.stepik.android.domain.personal_offers.model.OffersWrapper
import org.stepik.android.remote.personal_offers.mapper.OffersMapper
import org.stepik.android.remote.remote_storage.service.RemoteStorageService
import javax.inject.Inject

class OffersRemoteDataSourceImpl
@Inject
constructor(
    private val remoteStorageService: RemoteStorageService,
    private val offersMapper: OffersMapper,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : OffersRemoteDataSource {
    override fun getOfferRecord(): Maybe<StorageRecord<OffersWrapper>> =
        Single
            .fromCallable { sharedPreferenceHelper.profile?.id ?: -1 }
            .flatMapMaybe { userId ->
                remoteStorageService
                    .getStorageRecords(page = 1, userId = userId, kind = getKindOfRecord())
                    .map(offersMapper::mapToStorageRecord)
                    .toMaybe()
            }

    override fun createOffersRecord(): Single<StorageRecord<OffersWrapper>> =
        remoteStorageService
            .createStorageRecord(offersMapper.mapToStorageRequest())
            .map(offersMapper::mapToStorageRecord)
}