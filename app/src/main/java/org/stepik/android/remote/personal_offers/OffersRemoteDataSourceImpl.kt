package org.stepik.android.remote.personal_offers

import com.google.gson.JsonObject
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepic.droid.web.storage.model.StorageRecordWrapped
import org.stepik.android.data.personal_offers.source.OffersRemoteDataSource
import org.stepik.android.domain.personal_offers.model.OffersWrapper
import org.stepik.android.remote.personal_offers.mapper.OffersMapper
import org.stepik.android.remote.remote_storage.model.StorageRequest
import org.stepik.android.remote.remote_storage.service.RemoteStorageService
import javax.inject.Inject

class OffersRemoteDataSourceImpl
@Inject
constructor(
    private val remoteStorageService: RemoteStorageService,
    private val offersMapper: OffersMapper,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : OffersRemoteDataSource {
    companion object {
        private const val KIND_PERSONAL_OFFERS = "personal_offers"
    }
    override fun getOffersRecord(): Maybe<StorageRecord<OffersWrapper>> =
        Single
            .fromCallable { sharedPreferenceHelper.profile?.id ?: -1 }
            .flatMapMaybe { userId ->
                remoteStorageService
                    .getStorageRecords(page = 1, userId = userId, kind = KIND_PERSONAL_OFFERS)
                    .map(offersMapper::mapToStorageRecord)
                    .toMaybe()
            }

    override fun createOffersRecord(): Single<StorageRecord<OffersWrapper>> =
        remoteStorageService
            .createStorageRecord(
                StorageRequest(
                    StorageRecordWrapped(
                        id = null,
                        kind = KIND_PERSONAL_OFFERS,
                        data = JsonObject()
                    )
                )
            )
            .map(offersMapper::mapToStorageRecord)
}