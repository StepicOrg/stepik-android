package org.stepik.android.view.injection.network

import dagger.Binds
import dagger.Module
import org.stepik.android.data.network.repository.NetworkTypeRepositoryImpl
import org.stepik.android.domain.network.repository.NetworkTypeRepository

@Module
abstract class NetworkDataModule {
    @Binds
    internal abstract fun bindNetworkTypesRepository(
        networkTypeRepositoryImpl: NetworkTypeRepositoryImpl
    ): NetworkTypeRepository
}