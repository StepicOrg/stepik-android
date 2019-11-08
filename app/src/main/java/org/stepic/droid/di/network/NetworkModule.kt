package org.stepic.droid.di.network

import android.webkit.CookieManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.stepic.droid.configuration.Config
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.di.auth.AuthModule
import org.stepic.droid.features.achievements.repository.AchievementsRepository
import org.stepic.droid.features.achievements.repository.AchievementsRepositoryImpl
import org.stepic.droid.features.stories.repository.StoryTemplatesRepository
import org.stepic.droid.features.stories.repository.StoryTemplatesRepositoryImpl
import org.stepic.droid.util.DebugToolsHelper
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit

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

        @Provides
        @JvmStatic
        @AppSingleton
        internal fun provideCookieManager(): CookieManager =
            CookieManager.getInstance()

        @Provides
        @JvmStatic
        @AppSingleton
        @Authorized
        internal fun provideRetrofit(interceptors: Set<@JvmSuppressWildcards Interceptor>, config: Config): Retrofit {
            val okHttpBuilder = OkHttpClient.Builder()
                .connectTimeout(NetworkHelper.TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .readTimeout(NetworkHelper.TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            interceptors.forEach { okHttpBuilder.addNetworkInterceptor(it) }

            return Retrofit.Builder()
                .baseUrl(config.baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(NetworkHelper.createGsonConverterFactory())
                .client(okHttpBuilder.build())
                .build()
        }

    }
}