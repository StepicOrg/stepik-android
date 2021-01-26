package org.stepik.android.data.personal_offers.repository

import io.reactivex.Single
import org.stepic.droid.web.storage.model.StorageRecord
import org.stepik.android.data.personal_offers.source.PersonalOffersRemoteDataSource
import org.stepik.android.domain.personal_offers.model.PersonalOffersWrapper
import org.stepik.android.domain.personal_offers.repository.PersonalOffersRepository
import javax.inject.Inject

class PersonalOffersRepositoryImpl
@Inject
constructor(
    private val personalOffersRemoteDataSource: PersonalOffersRemoteDataSource
) : PersonalOffersRepository {
    override fun getPersonalOffers(): Single<StorageRecord<PersonalOffersWrapper>> =
        personalOffersRemoteDataSource
            .getPersonalOffers()
            .switchIfEmpty(createOffersRecord())

    private fun createOffersRecord(): Single<StorageRecord<PersonalOffersWrapper>> =
        personalOffersRemoteDataSource.createPersonalOffers()
}