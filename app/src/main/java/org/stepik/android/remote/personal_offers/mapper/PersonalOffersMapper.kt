package org.stepik.android.remote.personal_offers.mapper

import com.google.gson.Gson
import org.stepik.android.domain.personal_offers.model.PersonalOffers
import org.stepik.android.remote.remote_storage.model.StorageResponse
import javax.inject.Inject

class PersonalOffersMapper
@Inject
constructor(
    private val gson: Gson
) {
    fun mapToPersonalOffers(response: StorageResponse): PersonalOffers? =
        response
            .records
            .firstOrNull()
            ?.unwrap<PersonalOffers>(gson)
            ?.data
}