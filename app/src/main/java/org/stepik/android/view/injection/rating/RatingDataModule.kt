package org.stepik.android.view.injection.rating

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.stepic.droid.configuration.RemoteConfig
import org.stepik.android.remote.base.NetworkFactory
import org.stepik.android.data.rating.source.RatingRemoteDataSource
import org.stepik.android.remote.rating.RatingRemoteDataSourceImpl
import org.stepik.android.remote.rating.service.RatingService
import retrofit2.Converter

@Module
abstract class RatingDataModule {
    @Binds
    internal abstract fun bindRatingRemoteDataSource(
        ratingRemoteDataSourceImpl: RatingRemoteDataSourceImpl
    ): RatingRemoteDataSource

    @Module
    companion object {
        @Provides
        @JvmStatic
        internal fun provideRatingService(firebaseRemoteConfig: FirebaseRemoteConfig, okHttpClient: OkHttpClient, converterFactory: Converter.Factory): RatingService =
            NetworkFactory.createService(firebaseRemoteConfig.getString(RemoteConfig.ADAPTIVE_BACKEND_URL), okHttpClient, converterFactory)
    }
}