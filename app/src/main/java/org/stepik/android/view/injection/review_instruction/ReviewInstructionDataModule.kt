package org.stepik.android.view.injection.review_instruction

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.domain.review_instruction.repository.ReviewInstructionRepository
import org.stepik.android.data.review_instruction.repository.ReviewInstructionRepositoryImpl
import org.stepik.android.cache.review_instruction.ReviewInstructionCacheDataSourceImpl
import org.stepik.android.data.review_instruction.source.ReviewInstructionCacheDataSource
import org.stepik.android.cache.review_instruction.dao.ReviewInstructionDao
import org.stepik.android.remote.review_instruction.ReviewInstructionRemoteDataSourceImpl
import org.stepik.android.data.review_instruction.source.ReviewInstructionRemoteDataSource
import org.stepik.android.remote.review_instruction.service.ReviewInstructionService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.create

@Module
abstract class ReviewInstructionDataModule {
    @Binds
    internal abstract fun bindReviewInstructionRepository(
        reviewInstructionRepositoryImpl: ReviewInstructionRepositoryImpl
    ): ReviewInstructionRepository

    @Binds
    internal abstract fun bindReviewInstructionCacheDataSource(
        reviewInstructionCacheDataSourceImpl: ReviewInstructionCacheDataSourceImpl
    ): ReviewInstructionCacheDataSource

    @Binds
    internal abstract fun bindReviewInstructionRemoteDataSource(
        reviewInstructionRemoteDataSourceImpl: ReviewInstructionRemoteDataSourceImpl
    ): ReviewInstructionRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideReviewInstructionDao(appDatabase: AppDatabase): ReviewInstructionDao =
            appDatabase.reviewInstructionDao()

        @Provides
        @JvmStatic
        fun provideReviewInstructionService(@Authorized retrofit: Retrofit): ReviewInstructionService =
            retrofit.create()
    }
}