package org.stepik.android.view.injection.submission

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.submission.SubmissionCacheDataSourceImpl
import org.stepik.android.data.submission.repository.SubmissionRepositoryImpl
import org.stepik.android.data.submission.source.SubmissionCacheDataSource
import org.stepik.android.data.submission.source.SubmissionRemoteDataSource
import org.stepik.android.domain.submission.repository.SubmissionRepository
import org.stepik.android.remote.submission.SubmissionRemoteDataSourceImpl

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
}