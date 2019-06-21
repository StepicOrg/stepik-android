package org.stepik.android.view.injection.attempt

import dagger.Binds
import dagger.Module
import org.stepik.android.data.attempt.repository.AttemptRepositoryImpl
import org.stepik.android.data.attempt.source.AttemptRemoteDataSource
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.remote.attempt.AttemptRemoteDataSourceImpl

@Module
abstract class AttemptDataModule {
    @Binds
    internal abstract fun bindAttemptRepository(
        attemptRepositoryImpl: AttemptRepositoryImpl
    ): AttemptRepository

    @Binds
    internal abstract fun bindAttemptRemoteDataSource(
        attemptRemoteDataSourceImpl: AttemptRemoteDataSourceImpl
    ): AttemptRemoteDataSource
}