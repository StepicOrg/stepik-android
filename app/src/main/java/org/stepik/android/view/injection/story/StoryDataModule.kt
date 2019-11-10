package org.stepik.android.view.injection.story

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepik.android.data.stories.source.StoryTemplatesRemoteDataSource
import org.stepik.android.remote.stories.StoryTemplatesRemoteRemoteDataSourceImpl
import org.stepik.android.remote.stories.service.StoryService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit

@Module
abstract class StoryDataModule {
    @Binds
    internal abstract fun bindStoryTemplateRemoteDataSource(
        storyTemplatesRemoteRemoteDataSourceImpl: StoryTemplatesRemoteRemoteDataSourceImpl
    ): StoryTemplatesRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideStoryService(@Authorized retrofit: Retrofit): StoryService =
            retrofit.create(StoryService::class.java)
    }
}