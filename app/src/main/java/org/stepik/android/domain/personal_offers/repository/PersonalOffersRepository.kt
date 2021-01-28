package org.stepik.android.domain.personal_offers.repository

import io.reactivex.Single
import org.stepik.android.domain.personal_offers.model.PersonalOffers

interface PersonalOffersRepository {
    fun getPersonalOffers(): Single<PersonalOffers>
}