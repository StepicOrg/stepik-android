package org.stepik.android.data.personal_offers.source

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepik.android.domain.personal_offers.model.PersonalOffers

interface PersonalOffersRemoteDataSource {
    fun getPersonalOffers(): Maybe<PersonalOffers>
    fun createPersonalOffers(): Single<PersonalOffers>
}