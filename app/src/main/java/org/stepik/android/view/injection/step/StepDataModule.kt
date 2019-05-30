package org.stepik.android.view.injection.step

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.step.StepCacheDataSourceImpl
import org.stepik.android.data.step.repository.StepRepositoryImpl
import org.stepik.android.data.step.source.StepCacheDataSource
import org.stepik.android.data.step.source.StepRemoteDataSource
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.remote.step.StepRemoteDataSourceImpl

@Module
abstract class StepDataModule {
    @Binds
    internal abstract fun bindStepRepository(
        stepRepositoryImpl: StepRepositoryImpl
    ): StepRepository

    @Binds
    internal abstract fun bindStepCacheDataSource(
        stepCacheDataSourceImpl: StepCacheDataSourceImpl
    ): StepCacheDataSource

    @Binds
    internal abstract fun bindStepRemoteDataSource(
        stepRemoteDataSourceImpl: StepRemoteDataSourceImpl
    ): StepRemoteDataSource
}