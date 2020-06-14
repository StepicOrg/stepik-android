package org.stepik.android.view.injection.step

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.step.StepCacheDataSourceImpl
import org.stepik.android.data.step.repository.StepRepositoryImpl
import org.stepik.android.data.step.source.StepCacheDataSource
import org.stepik.android.data.step.source.StepRemoteDataSource
import org.stepik.android.domain.step.repository.StepRepository
import org.stepik.android.remote.step.StepRemoteDataSourceImpl
import org.stepik.android.remote.step.service.StepService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

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

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideStepService(@Authorized retrofit: Retrofit): StepService =
            retrofit.create(StepService::class.java)
    }
}