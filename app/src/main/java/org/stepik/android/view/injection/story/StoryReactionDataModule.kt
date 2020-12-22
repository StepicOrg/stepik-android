package org.stepik.android.view.injection.story

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.story.StoryReactionCacheDataSourceImpl
import org.stepik.android.cache.story.dao.StoryReactionDao
import org.stepik.android.data.story.repository.StoryReactionRepositoryImpl
import org.stepik.android.data.story.source.StoryReactionCacheDataSource
import org.stepik.android.domain.story.repository.StoryReactionRepository

@Module
internal abstract class StoryReactionDataModule {
    @Binds
    internal abstract fun bindStoryReactionRepository(
        storyReactionRepositoryImpl: StoryReactionRepositoryImpl
    ): StoryReactionRepository

    @Binds
    internal abstract fun bindStoryReactionCacheDataSource(
        storyReactionCacheDataSourceImpl: StoryReactionCacheDataSourceImpl
    ): StoryReactionCacheDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        fun provideStoryReactionDao(appDatabase: AppDatabase): StoryReactionDao =
            appDatabase.storyReactionDao()
    }
}