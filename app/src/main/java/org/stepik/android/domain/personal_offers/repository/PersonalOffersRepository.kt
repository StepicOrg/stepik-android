package org.stepik.android.domain.personal_offers.repository

import org.stepik.android.domain.personal_offers.model.PersonalOffers

interface PersonalOffersRepository {
    suspend fun getPersonalOffers(): PersonalOffers
}