package org.stepik.android.view.injection.personal_offers

import dagger.Binds
import dagger.Module
import org.stepik.android.data.personal_offers.repository.OffersRepositoryImpl
import org.stepik.android.data.personal_offers.source.OffersRemoteDataSource
import org.stepik.android.domain.personal_offers.repository.OffersRepository
import org.stepik.android.remote.personal_offers.OffersRemoteDataSourceImpl
import org.stepik.android.view.injection.remote_storage.RemoteStorageDataModule

@Module(includes = [RemoteStorageDataModule::class])
abstract class PersonalOffersDataModule {
    @Binds
    internal abstract fun bindOffersRepository(
        offersRepositoryImpl: OffersRepositoryImpl
    ): OffersRepository

    @Binds
    internal abstract fun bindOffersRemoteDataSource(
        offersRemoteDataSourceImpl: OffersRemoteDataSourceImpl
    ): OffersRemoteDataSource
}