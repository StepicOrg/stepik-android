package org.stepik.android.view.injection.unit

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.unit.UnitCacheDataSourceImpl
import org.stepik.android.data.unit.repository.UnitRepositoryImpl
import org.stepik.android.data.unit.source.UnitCacheDataSource
import org.stepik.android.data.unit.source.UnitRemoteDataSource
import org.stepik.android.domain.unit.repository.UnitRepository
import org.stepik.android.remote.unit.UnitRemoteDataSourceImpl

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
}