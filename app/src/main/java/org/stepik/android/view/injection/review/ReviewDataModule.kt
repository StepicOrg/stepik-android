package org.stepik.android.view.injection.review

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.domain.review.repository.ReviewRepository
import org.stepik.android.data.review.repository.ReviewRepositoryImpl
import org.stepik.android.remote.review.ReviewRemoteDataSourceImpl
import org.stepik.android.data.review.source.ReviewRemoteDataSource
import org.stepik.android.remote.review.service.ReviewService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class ReviewDataModule {
    @Binds
    internal abstract fun bindReviewRepository(
        reviewRepositoryImpl: ReviewRepositoryImpl
    ): ReviewRepository

    @Binds
    internal abstract fun bindReviewRemoteDataSource(
        reviewRemoteDataSourceImpl: ReviewRemoteDataSourceImpl
    ): ReviewRemoteDataSource

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideReviewService(@Authorized retrofit: Retrofit): ReviewService =
            retrofit.create()
    }
}