package org.stepik.android.view.injection.progress

import dagger.Binds
import dagger.Module
import org.stepik.android.data.progress.repository.ProgressRepositoryImpl
import org.stepik.android.data.progress.source.ProgressRemoteDataSource
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.remote.progress.ProgressRemoteDataSourceImpl

@Module
abstract class ProgressModule {

    @Binds
    internal abstract fun bindProgressRepository(
        progressRepositoryImpl: ProgressRepositoryImpl): ProgressRepository

    @Binds
    internal abstract fun bindProgressRemoteDataSource(
        progressRemoteDataSourceImpl: ProgressRemoteDataSourceImpl): ProgressRemoteDataSource

}