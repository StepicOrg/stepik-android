package org.stepik.android.view.injection.progress

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.progress.ProgressCacheDataSourceImpl
import org.stepik.android.data.progress.repository.ProgressRepositoryImpl
import org.stepik.android.data.progress.source.ProgressCacheDataSource
import org.stepik.android.data.progress.source.ProgressRemoteDataSource
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.remote.progress.ProgressRemoteDataSourceImpl
import org.stepik.android.remote.progress.service.ProgressService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class ProgressDataModule {
    @Binds
    internal abstract fun bindProgressRepository(
        progressRepositoryImpl: ProgressRepositoryImpl
    ): ProgressRepository

    @Binds
    internal abstract fun bindProgressRemoteDataSource(
        progressRemoteDataSourceImpl: ProgressRemoteDataSourceImpl
    ): ProgressRemoteDataSource

    @Binds
    internal abstract fun bindProgressCacheDataSource(
        progressCacheDataSourceImpl: ProgressCacheDataSourceImpl
    ): ProgressCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideProgressService(@Authorized retrofit: Retrofit): ProgressService =
            retrofit.create(ProgressService::class.java)
    }
}