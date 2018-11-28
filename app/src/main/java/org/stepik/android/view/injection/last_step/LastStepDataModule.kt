package org.stepik.android.view.injection.last_step

import dagger.Binds
import dagger.Module
import org.stepik.android.data.last_step.repository.LastStepRepositoryImpl
import org.stepik.android.data.last_step.source.LastStepRemoteDataSource
import org.stepik.android.domain.last_step.repository.LastStepRepository
import org.stepik.android.remote.last_step.LastStepRemoteDataSourceImpl

@Module
abstract class LastStepDataModule {
    @Binds
    internal abstract fun bindLastStepRepository(
        lastStepRepositoryImpl: LastStepRepositoryImpl
    ): LastStepRepository

    @Binds
    internal abstract fun bindLastStepRemoteDataSource(
        lastStepRemoteDataSourceImpl: LastStepRemoteDataSourceImpl
    ): LastStepRemoteDataSource

//    @Binds
//    internal abstract fun bindProgressCacheDataSource(
//        progressCacheDataSourceImpl: ProgressCacheDataSourceImpl
//    ): ProgressCacheDataSource
}