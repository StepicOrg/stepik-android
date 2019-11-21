package org.stepik.android.view.injection.step_source

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.step_source.repository.StepSourceRepositoryImpl
import org.stepik.android.data.step_source.source.StepSourceRemoteDataSource
import org.stepik.android.domain.step_source.repository.StepSourceRepository
import org.stepik.android.remote.step_source.StepSourceRemoteDataSourceImpl
import org.stepik.android.remote.step_source.service.StepSourceService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
internal abstract class StepSourceDataModule {
    @Binds
    internal abstract fun bindStepSourceRepository(
        stepSourceRepositoryImpl: StepSourceRepositoryImpl
    ): StepSourceRepository

    @Binds
    internal abstract fun bindStepSourceRemoteDataSource(
        stepSourceRemoteDataSourceImpl: StepSourceRemoteDataSourceImpl
    ): StepSourceRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideStepSourceService(@Authorized retrofit: Retrofit): StepSourceService =
            retrofit.create(StepSourceService::class.java)
    }
}