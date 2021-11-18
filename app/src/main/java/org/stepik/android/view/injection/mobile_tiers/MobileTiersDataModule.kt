package org.stepik.android.view.injection.mobile_tiers

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.mobile_tiers.LightSkuCacheDataSourceImpl
import org.stepik.android.cache.mobile_tiers.MobileTiersCacheDataSourceImpl
import org.stepik.android.cache.mobile_tiers.dao.LightSkuDao
import org.stepik.android.cache.mobile_tiers.dao.MobileTiersDao
import org.stepik.android.data.mobile_tiers.repository.LightSkuRepositoryImpl
import org.stepik.android.data.mobile_tiers.repository.MobileTiersRepositoryImpl
import org.stepik.android.data.mobile_tiers.source.LightSkuCacheDataSource
import org.stepik.android.data.mobile_tiers.source.MobileTiersCacheDataSource
import org.stepik.android.data.mobile_tiers.source.MobileTiersRemoteDataSource
import org.stepik.android.domain.mobile_tiers.repository.LightSkuRepository
import org.stepik.android.domain.mobile_tiers.repository.MobileTiersRepository
import org.stepik.android.remote.mobile_tiers.MobileTiersRemoteDataSourceImpl
import org.stepik.android.remote.mobile_tiers.service.MobileTiersService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class MobileTiersDataModule {
    @Binds
    internal abstract fun bindMobileTiersRepository(
        mobileTiersRepositoryImpl: MobileTiersRepositoryImpl
    ): MobileTiersRepository

    @Binds
    internal abstract fun bindMobileTiersRemoteDataSource(
        mobileTiersRemoteDataSourceImpl: MobileTiersRemoteDataSourceImpl
    ): MobileTiersRemoteDataSource

    @Binds
    internal abstract fun bindMobileTiersCacheDataSource(
        mobileTiersCacheDataSourceImpl: MobileTiersCacheDataSourceImpl
    ): MobileTiersCacheDataSource

    @Binds
    internal abstract fun bindLightSkuRepository(
        lightSkuRepositoryImpl: LightSkuRepositoryImpl
    ): LightSkuRepository

    @Binds
    internal abstract fun bindLightSkuCacheDataSource(
        lightSkuCacheDataSourceImpl: LightSkuCacheDataSourceImpl
    ): LightSkuCacheDataSource

    companion object {
        @Provides
        @JvmStatic
        fun provideLightSkuDao(appDatabase: AppDatabase): LightSkuDao =
            appDatabase.lightSkuDao()

        @Provides
        @JvmStatic
        fun provideMobileTiersDao(appDatabase: AppDatabase): MobileTiersDao =
            appDatabase.mobileTiersDao()

        @Provides
        @JvmStatic
        fun provideMobileTiersService(@Authorized retrofit: Retrofit): MobileTiersService =
            retrofit.create()
    }
}