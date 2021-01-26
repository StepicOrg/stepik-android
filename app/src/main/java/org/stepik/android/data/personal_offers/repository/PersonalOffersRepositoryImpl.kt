package org.stepik.android.data.personal_offers.repository

import io.reactivex.Single
import org.stepik.android.data.personal_offers.source.PersonalOffersRemoteDataSource
import org.stepik.android.domain.personal_offers.model.PersonalOffers
import org.stepik.android.domain.personal_offers.repository.PersonalOffersRepository
import javax.inject.Inject

class PersonalOffersRepositoryImpl
@Inject
constructor(
    private val personalOffersRemoteDataSource: PersonalOffersRemoteDataSource
) : PersonalOffersRepository {
    override fun getPersonalOffers(): Single<PersonalOffers> =
        personalOffersRemoteDataSource
            .getPersonalOffers()
            .switchIfEmpty(createOffersRecord())

    private fun createOffersRecord(): Single<PersonalOffers> =
        personalOffersRemoteDataSource.createPersonalOffers()
}