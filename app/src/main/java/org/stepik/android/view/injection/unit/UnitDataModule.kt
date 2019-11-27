package org.stepik.android.view.injection.unit

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.unit.UnitCacheDataSourceImpl
import org.stepik.android.data.unit.repository.UnitRepositoryImpl
import org.stepik.android.data.unit.source.UnitCacheDataSource
import org.stepik.android.data.unit.source.UnitRemoteDataSource
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.remote.unit.UnitRemoteDataSourceImpl
import org.stepik.android.remote.unit.service.UnitService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class UnitDataModule {
    @Binds
    internal abstract fun bindUnitRepository(
        unitRepositoryImpl: UnitRepositoryImpl
    ): UnitRepository

    @Binds
    internal abstract fun bindUnitCacheDataSource(
        unitCacheDataSourceImpl: UnitCacheDataSourceImpl
    ): UnitCacheDataSource

    @Binds
    internal abstract fun bindUnitRemoteDataSource(
        unitRemoteDataSourceImpl: UnitRemoteDataSourceImpl
    ): UnitRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideUnitService(@Authorized retrofit: Retrofit): UnitService =
            retrofit.create(UnitService::class.java)
    }
}