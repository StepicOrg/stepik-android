package org.stepik.android.view.injection.feedback

import dagger.Binds
import dagger.Module
import org.stepik.android.cache.feedback.FeedbackCacheDataSourceImpl
import org.stepik.android.data.feedback.repository.FeedbackRepositoryImpl
import org.stepik.android.data.feedback.source.FeedbackCacheDataSource
import org.stepik.android.domain.feedback.repository.FeedbackRepository

@Module
abstract class FeedbackDataModule {
    @Binds
    internal abstract fun bindFeedbackRepository(
        feedbackRepositoryImpl: FeedbackRepositoryImpl
    ): FeedbackRepository

    @Binds
    internal abstract fun bindFeedbackCacheDataSource(
        feedbackCacheDataSourceImpl: FeedbackCacheDataSourceImpl
    ): FeedbackCacheDataSource
}