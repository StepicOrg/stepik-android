package org.stepic.droid.di.network

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import org.stepic.droid.configuration.Config
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.web.RatingService
import org.stepic.droid.web.StepicRestLoggedService
import org.stepic.droid.web.achievements.AchievementsService
import org.stepic.droid.web.storage.RemoteStorageService

@Module
abstract class ServicesModule {
    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideAchievementService(interceptors: Set<@JvmSuppressWildcards Interceptor>, config: Config): AchievementsService =
            NetworkHelper.createService(interceptors, config.baseUrl)

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideRemoteStorageService(interceptors: Set<@JvmSuppressWildcards Interceptor>, config: Config): RemoteStorageService =
            NetworkHelper.createService(interceptors, config.baseUrl)

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideRatingService(interceptors: Set<@JvmSuppressWildcards Interceptor>, firebaseRemoteConfig: FirebaseRemoteConfig): RatingService =
            NetworkHelper.createService(interceptors, firebaseRemoteConfig.getString(RemoteConfig.ADAPTIVE_BACKEND_URL))

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideStepikService(interceptors: Set<@JvmSuppressWildcards Interceptor>, config: Config): StepicRestLoggedService =
            NetworkHelper.createService(interceptors, config.baseUrl)
    }
}