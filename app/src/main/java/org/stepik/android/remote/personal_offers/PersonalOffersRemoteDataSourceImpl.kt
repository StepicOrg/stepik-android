package org.stepik.android.remote.personal_offers

import com.google.gson.JsonObject
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
    override suspend fun getPersonalOffers(): PersonalOffers? {
        val userId = sharedPreferenceHelper.profile?.id ?: -1
        return remoteStorageService
            .getStorageRecordsCoroutine(page = 1, userId = userId, kind = KIND_PERSONAL_OFFERS)
            .let(personalOffersMapper::mapToPersonalOffers)
    }

    override suspend fun createPersonalOffers(): PersonalOffers =
        remoteStorageService
            .createStorageRecordCoroutine(
                StorageRequest(
                    StorageRecordWrapped(
                        id = null,
                        kind = KIND_PERSONAL_OFFERS,
                        data = JsonObject()
                    )
                )
            )
            .let(personalOffersMapper::mapToPersonalOffers)!!
}