package org.stepik.android.data.personal_offers.source

import io.reactivex.Single
import org.stepik.android.domain.personal_offers.model.PersonalOffers

interface PersonalOffersRemoteDataSource {
    fun getPersonalOffers(): Single<PersonalOffers>
    fun createPersonalOffers(): Single<PersonalOffers>
}