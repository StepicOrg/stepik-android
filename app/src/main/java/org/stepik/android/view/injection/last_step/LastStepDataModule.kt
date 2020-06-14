package org.stepik.android.view.injection.last_step

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.last_step.LastStepCacheDataSourceImpl
import org.stepik.android.data.last_step.repository.LastStepRepositoryImpl
import org.stepik.android.data.last_step.source.LastStepCacheDataSource
import org.stepik.android.data.last_step.source.LastStepRemoteDataSource
import org.stepik.android.domain.last_step.repository.LastStepRepository
import org.stepik.android.remote.last_step.LastStepRemoteDataSourceImpl
import org.stepik.android.remote.last_step.service.LastStepService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

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

    @Binds
    internal abstract fun bindProgressCacheDataSource(
        lastStepCacheDataSourceImpl: LastStepCacheDataSourceImpl
    ): LastStepCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideLessonService(@Authorized retrofit: Retrofit): LastStepService =
            retrofit.create(LastStepService::class.java)
    }
}