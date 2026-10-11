package org.stepik.android.view.injection.features

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.di.AppSingleton
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
    @AppSingleton
    internal abstract fun bindFeaturesRepository(
        featuresRepositoryImpl: FeaturesRepositoryImpl
    ): FeaturesRepository

    @Binds
    @AppSingleton
    internal abstract fun bindFeaturesRemoteDataSource(
        featuresRemoteDataSourceImpl: FeaturesRemoteDataSourceImpl
    ): FeaturesRemoteDataSource

    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideFeaturesService(@Authorized retrofit: Retrofit): FeaturesService =
            retrofit.create(FeaturesService::class.java)
    }
}
