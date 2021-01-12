package org.stepik.android.view.injection.catalog

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.catalog.CatalogCacheDataSourceImpl
import org.stepik.android.cache.catalog.dao.CatalogBlockDao
import org.stepik.android.data.catalog.repository.CatalogRepositoryImpl
import org.stepik.android.data.catalog.source.CatalogCacheDataSource
import org.stepik.android.data.catalog.source.CatalogRemoteDataSource
import org.stepik.android.domain.catalog.repository.CatalogRepository
import org.stepik.android.remote.catalog.CatalogRemoteDataSourceImpl
import org.stepik.android.remote.catalog.service.CatalogService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class CatalogDataModule {
    @Binds
    internal abstract fun bindCatalogBlockRepository(
        catalogBlockRepositoryImpl: CatalogRepositoryImpl
    ): CatalogRepository

    @Binds
    internal abstract fun bindCatalogBlockCacheDataSource(
        catalogBlockCacheDataSourceImpl: CatalogCacheDataSourceImpl
    ): CatalogCacheDataSource

    @Binds
    internal abstract fun bindCatalogBlockRemoteDataSource(
        catalogRemoteDataSourceImpl: CatalogRemoteDataSourceImpl
    ): CatalogRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideCatalogBlockDao(appDatabase: AppDatabase): CatalogBlockDao =
            appDatabase.catalogBlockDao()

        @Provides
        @JvmStatic
        internal fun provideCatalogBlockService(@Authorized retrofit: Retrofit): CatalogService =
            retrofit.create()
    }
}