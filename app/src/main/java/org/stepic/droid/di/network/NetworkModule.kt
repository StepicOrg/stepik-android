package org.stepic.droid.di.network

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.auth.AuthModule
import org.stepic.droid.features.achievements.repository.AchievementsRepository
import org.stepic.droid.features.achievements.repository.AchievementsRepositoryImpl
import org.stepic.droid.features.stories.repository.StoryTemplatesRepository
import org.stepic.droid.features.stories.repository.StoryTemplatesRepositoryImpl
import org.stepic.droid.util.DebugToolsHelper

@Module(includes = [AuthModule::class, ServicesModule::class])
abstract class NetworkModule {

    @Binds
    @AppSingleton
    abstract fun bindAchievementsRepository(achievementsRepositoryImpl: AchievementsRepositoryImpl): AchievementsRepository

    @Binds
    abstract fun bindStoryTemplatesRepository(storyTemplatesRepositoryImpl: StoryTemplatesRepositoryImpl): StoryTemplatesRepository

    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        @IntoSet
        @DebugInterceptors
        fun provideStethoInterceptor(): List<Interceptor> =
            DebugToolsHelper.getDebugInterceptors()
    }
}