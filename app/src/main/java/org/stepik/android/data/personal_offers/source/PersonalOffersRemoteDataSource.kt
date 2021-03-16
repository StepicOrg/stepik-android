package org.stepik.android.data.personal_offers.source

import org.stepik.android.domain.personal_offers.model.PersonalOffers

interface PersonalOffersRemoteDataSource {
    suspend fun getPersonalOffers(): PersonalOffers?
    suspend fun createPersonalOffers(): PersonalOffers
}