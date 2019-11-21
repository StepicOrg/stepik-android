package org.stepik.android.view.injection.story

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.features.stories.repository.StoryTemplatesRepository
import org.stepic.droid.features.stories.repository.StoryTemplatesRepositoryImpl
import org.stepik.android.data.stories.source.StoryTemplatesRemoteDataSource
import org.stepik.android.remote.stories.StoryTemplatesRemoteDataSourceImpl
import org.stepik.android.remote.stories.service.StoryService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class StoryDataModule {
    @Binds
    abstract fun bindStoryTemplatesRepository(
        storyTemplatesRepositoryImpl: StoryTemplatesRepositoryImpl
    ): StoryTemplatesRepository

    @Binds
    internal abstract fun bindStoryTemplateRemoteDataSource(
        storyTemplatesRemoteDataSourceImpl: StoryTemplatesRemoteDataSourceImpl
    ): StoryTemplatesRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideStoryService(@Authorized retrofit: Retrofit): StoryService =
            retrofit.create(StoryService::class.java)
    }
}