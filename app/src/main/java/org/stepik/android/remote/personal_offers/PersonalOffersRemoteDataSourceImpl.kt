package org.stepik.android.remote.personal_offers

import com.google.gson.JsonObject
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.web.storage.model.StorageRecordWrapped
import org.stepik.android.data.personal_offers.source.PersonalOffersRemoteDataSource
import org.stepik.android.domain.personal_offers.model.PersonalOffers
import org.stepik.android.remote.personal_offers.mapper.PersonalOffersMapper
import org.stepik.android.remote.remote_storage.model.StorageRequest
import org.stepik.android.remote.remote_storage.service.RemoteStorageService
import javax.inject.Inject

class PersonalOffersRemoteDataSourceImpl
@Inject
constructor(
    private val remoteStorageService: RemoteStorageService,
    private val personalOffersMapper: PersonalOffersMapper,
    private val sharedPreferenceHelper: SharedPreferenceHelper
) : PersonalOffersRemoteDataSource {
    companion object {
        private const val KIND_PERSONAL_OFFERS = "personal_offers"
    }
    override fun getPersonalOffers(): Maybe<PersonalOffers> =
        Single
            .fromCallable { sharedPreferenceHelper.profile?.id ?: -1 }
            .flatMapMaybe { userId ->
                remoteStorageService
                    .getStorageRecords(page = 1, userId = userId, kind = KIND_PERSONAL_OFFERS)
                    .map(personalOffersMapper::mapToPersonalOffers)
                    .toMaybe()
            }

    override fun createPersonalOffers(): Single<PersonalOffers> =
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
            .map(personalOffersMapper::mapToPersonalOffers)
}