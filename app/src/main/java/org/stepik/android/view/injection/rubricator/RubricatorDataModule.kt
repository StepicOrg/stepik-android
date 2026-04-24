package org.stepik.android.view.injection.rubricator

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.rubricator.repository.RubricatorRepositoryImpl
import org.stepik.android.data.rubricator.source.RubricatorRemoteDataSource
import org.stepik.android.domain.rubricator.repository.RubricatorRepository
import org.stepik.android.remote.rubricator.RubricatorRemoteDataSourceImpl
import org.stepik.android.remote.rubricator.service.RubricatorService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class RubricatorDataModule {
    @Binds
    internal abstract fun bindRubricatorRepository(
        rubricatorRepositoryImpl: RubricatorRepositoryImpl
    ): RubricatorRepository

    @Binds
    internal abstract fun bindRubricatorRemoteDataSource(
        rubricatorRemoteDataSourceImpl: RubricatorRemoteDataSourceImpl
    ): RubricatorRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideRubricatorService(@Authorized retrofit: Retrofit): RubricatorService =
            retrofit.create(RubricatorService::class.java)
    }
}