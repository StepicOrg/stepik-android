package org.stepik.android.view.injection.catalog_block

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.catalog_block.repository.CatalogBlockRepositoryImpl
import org.stepik.android.data.catalog_block.source.CatalogBlockRemoteDataSource
import org.stepik.android.domain.catalog_block.repository.CatalogBlockRepository
import org.stepik.android.remote.catalog_block.CatalogBlockRemoteDataSourceImpl
import org.stepik.android.remote.catalog_block.service.CatalogBlockService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class CatalogBlockDataModule {
    @Binds
    internal abstract fun bindCatalogBlockRepository(
        catalogBlockRepositoryImpl: CatalogBlockRepositoryImpl
    ): CatalogBlockRepository

    @Binds
    internal abstract fun bindCatalogBlockRemoteDataSource(
        catalogBlockRemoteDataSourceImpl: CatalogBlockRemoteDataSourceImpl
    ): CatalogBlockRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideCatalogBlockService(@Authorized retrofit: Retrofit): CatalogBlockService =
            retrofit.create()
    }
}