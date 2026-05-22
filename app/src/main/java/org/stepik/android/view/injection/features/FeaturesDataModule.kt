package org.stepik.android.view.injection.features

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.features.repository.FeaturesRepositoryImpl
import org.stepik.android.data.features.source.FeaturesRemoteDataSource
import org.stepik.android.domain.feature.repository.FeaturesRepository
import org.stepik.android.remote.features.FeaturesRemoteDataSourceImpl
import org.stepik.android.remote.features.service.FeaturesService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class FeaturesDataModule {
    @Binds
    internal abstract fun bindFeaturesRepository(
        featuresRepositoryImpl: FeaturesRepositoryImpl
    ): FeaturesRepository

    @Binds
    internal abstract fun bindFeaturesRemoteDataSource(
        featuresRemoteDataSourceImpl: FeaturesRemoteDataSourceImpl
    ): FeaturesRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideFeaturesService(@Authorized retrofit: Retrofit): FeaturesService =
            retrofit.create(FeaturesService::class.java)
    }
}