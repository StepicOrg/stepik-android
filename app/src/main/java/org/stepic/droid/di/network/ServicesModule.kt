package org.stepic.droid.di.network

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.stepic.droid.configuration.RemoteConfig
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.web.NetworkFactory
import org.stepic.droid.web.RatingService
import org.stepic.droid.web.StepicRestLoggedService
import org.stepic.droid.web.achievements.AchievementsService
import org.stepic.droid.web.storage.RemoteStorageService
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Converter
import retrofit2.Retrofit

@Module
abstract class ServicesModule {
    @Module
    companion object {
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideAchievementService(@Authorized retrofit: Retrofit): AchievementsService =
            retrofit.create(AchievementsService::class.java)

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideRemoteStorageService(@Authorized retrofit: Retrofit): RemoteStorageService =
            retrofit.create(RemoteStorageService::class.java)

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideStepikService(@Authorized retrofit: Retrofit): StepicRestLoggedService =
            retrofit.create(StepicRestLoggedService::class.java)

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideRatingService(firebaseRemoteConfig: FirebaseRemoteConfig, okHttpClient: OkHttpClient, converterFactory: Converter.Factory): RatingService =
            NetworkFactory.createService(firebaseRemoteConfig.getString(RemoteConfig.ADAPTIVE_BACKEND_URL), okHttpClient, converterFactory)
    }
}