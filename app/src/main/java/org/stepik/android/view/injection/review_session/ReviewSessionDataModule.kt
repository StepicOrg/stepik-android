package org.stepik.android.view.injection.review_session

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.domain.review_session.repository.ReviewSessionRepository
import org.stepik.android.data.review_session.repository.ReviewSessionRepositoryImpl
import org.stepik.android.cache.review_session.ReviewSessionCacheDataSourceImpl
import org.stepik.android.data.review_session.source.ReviewSessionCacheDataSource
import org.stepik.android.cache.review_session.dao.ReviewSessionDao
import org.stepik.android.remote.review_session.ReviewSessionRemoteDataSourceImpl
import org.stepik.android.data.review_session.source.ReviewSessionRemoteDataSource
import org.stepik.android.remote.review_session.service.ReviewSessionService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class ReviewSessionDataModule {
    @Binds
    internal abstract fun bindReviewSessionRepository(
        reviewSessionRepositoryImpl: ReviewSessionRepositoryImpl
    ): ReviewSessionRepository

    @Binds
    internal abstract fun bindReviewSessionCacheDataSource(
        reviewSessionCacheDataSourceImpl: ReviewSessionCacheDataSourceImpl
    ): ReviewSessionCacheDataSource

    @Binds
    internal abstract fun bindReviewSessionRemoteDataSource(
        reviewSessionRemoteDataSourceImpl: ReviewSessionRemoteDataSourceImpl
    ): ReviewSessionRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideReviewSessionDao(appDatabase: AppDatabase): ReviewSessionDao =
            appDatabase.reviewSessionDao()

        @Provides
        @JvmStatic
        fun provideReviewSessionService(@Authorized retrofit: Retrofit): ReviewSessionService =
            retrofit.create()
    }
}