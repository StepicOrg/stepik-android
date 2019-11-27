package org.stepik.android.view.injection.submission

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.submission.SubmissionCacheDataSourceImpl
import org.stepik.android.data.submission.repository.SubmissionRepositoryImpl
import org.stepik.android.data.submission.source.SubmissionCacheDataSource
import org.stepik.android.data.submission.source.SubmissionRemoteDataSource
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.remote.submission.SubmissionRemoteDataSourceImpl
import org.stepik.android.remote.submission.service.SubmissionService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class SubmissionDataModule {
    @Binds
    internal abstract fun bindSubmissionRepository(
        submissionRepositoryImpl: SubmissionRepositoryImpl
    ): SubmissionRepository

    @Binds
    internal abstract fun bindSubmissionRemoteDataSource(
        submissionRemoteDataSourceImpl: SubmissionRemoteDataSourceImpl
    ): SubmissionRemoteDataSource

    @Binds
    internal abstract fun bindSubmissionCacheDataSource(
        submissionCacheDataSourceImpl: SubmissionCacheDataSourceImpl
    ): SubmissionCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideSubmissionService(@Authorized retrofit: Retrofit): SubmissionService =
            retrofit.create(SubmissionService::class.java)
    }
}