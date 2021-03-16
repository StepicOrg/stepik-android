package org.stepik.android.data.personal_offers.repository

import org.stepik.android.data.personal_offers.source.PersonalOffersRemoteDataSource
import org.stepik.android.domain.personal_offers.model.PersonalOffers
import org.stepik.android.domain.personal_offers.repository.PersonalOffersRepository
import javax.inject.Inject

class PersonalOffersRepositoryImpl
@Inject
constructor(
    private val personalOffersRemoteDataSource: PersonalOffersRemoteDataSource
) : PersonalOffersRepository {
    override suspend fun getPersonalOffers(): PersonalOffers =
        personalOffersRemoteDataSource.getPersonalOffers()
            ?: createOffersRecord()

    private suspend fun createOffersRecord(): PersonalOffers =
        personalOffersRemoteDataSource.createPersonalOffers()
}