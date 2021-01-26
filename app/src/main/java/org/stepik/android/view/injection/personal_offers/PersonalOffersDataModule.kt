package org.stepik.android.view.injection.personal_offers

import dagger.Binds
import dagger.Module
import org.stepik.android.data.personal_offers.repository.PersonalOffersRepositoryImpl
import org.stepik.android.data.personal_offers.source.PersonalOffersRemoteDataSource
import org.stepik.android.domain.personal_offers.repository.PersonalOffersRepository
import org.stepik.android.remote.personal_offers.PersonalOffersRemoteDataSourceImpl
import org.stepik.android.view.injection.remote_storage.RemoteStorageDataModule

@Module(includes = [RemoteStorageDataModule::class])
abstract class PersonalOffersDataModule {
    @Binds
    internal abstract fun bindOffersRepository(
        personalOffersRepositoryImpl: PersonalOffersRepositoryImpl
    ): PersonalOffersRepository

    @Binds
    internal abstract fun bindOffersRemoteDataSource(
        offersRemoteDataSourceImpl: PersonalOffersRemoteDataSourceImpl
    ): PersonalOffersRemoteDataSource
}