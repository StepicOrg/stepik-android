package org.stepik.android.view.injection.attempt

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.attempt.repository.AttemptRepositoryImpl
import org.stepik.android.data.attempt.source.AttemptRemoteDataSource
import org.stepik.android.domain.attempt.repository.AttemptRepository
import org.stepik.android.remote.attempt.AttemptRemoteDataSourceImpl
import org.stepik.android.remote.attempt.service.AttemptService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

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

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideAttemptService(@Authorized retrofit: Retrofit): AttemptService =
            retrofit.create(AttemptService::class.java)
    }
}