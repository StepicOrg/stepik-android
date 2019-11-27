package org.stepik.android.view.injection.recommendation

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.recommendation.repository.RecommendationRepositoryImpl
import org.stepik.android.data.recommendation.source.RecommendationRemoteDataSource
import org.stepik.android.domain.recommendation.repository.RecommendationRepository
import org.stepik.android.remote.recommendation.RecommendationRemoteDataSourceImpl
import org.stepik.android.remote.recommendation.service.RecommendationService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class RecommendationModule {
    @Binds
    internal abstract fun bindRecommendationRepository(
        recommendationRepositoryImpl: RecommendationRepositoryImpl
    ): RecommendationRepository

    @Binds
    internal abstract fun bindRecommendationRemoteDataSource(
        recommendationRemoteDataSourceImpl: RecommendationRemoteDataSourceImpl
    ): RecommendationRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideRecommendationService(@Authorized retrofit: Retrofit): RecommendationService =
            retrofit.create(RecommendationService::class.java)
    }
}