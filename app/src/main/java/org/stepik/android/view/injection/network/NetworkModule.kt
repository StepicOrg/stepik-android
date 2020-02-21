package org.stepik.android.view.injection.network

import android.webkit.CookieManager
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.stepic.droid.configuration.Config
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.util.DebugToolsHelper
import org.stepik.android.view.injection.achievement.AchievementDataModule
import org.stepik.android.view.injection.auth.AuthDataModule
import org.stepik.android.view.injection.base.Authorized
import org.stepik.android.view.injection.qualifiers.DebugInterceptors
import org.stepik.android.view.injection.serialization.SerializationModule
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit

@Module(includes = [AuthDataModule::class, SerializationModule::class, AchievementDataModule::class])
abstract class NetworkModule {

    @Module
    companion object {
        const val TIMEOUT_IN_SECONDS = 60L

        @Provides
        @AppSingleton
        @JvmStatic
        @DebugInterceptors
        fun provideDebugInterceptors(): List<Interceptor> =
            DebugToolsHelper.getDebugInterceptors()

        @Provides
        @JvmStatic
        @AppSingleton
        internal fun provideCookieManager(): CookieManager =
            CookieManager.getInstance()

        @Provides
        @JvmStatic
        @AppSingleton
        internal fun provideOkHttpClient(
            @DebugInterceptors debugInterceptors: List<@JvmSuppressWildcards Interceptor>,
            interceptors: Set<@JvmSuppressWildcards Interceptor>
        ): OkHttpClient {
            val okHttpBuilder = OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            interceptors.forEach { okHttpBuilder.addNetworkInterceptor(it) }
            debugInterceptors.forEach { okHttpBuilder.addNetworkInterceptor(it) }

            return okHttpBuilder.build()
        }

        @Provides
        @JvmStatic
        @AppSingleton
        @Authorized
        internal fun provideRetrofit(config: Config, okHttpClient: OkHttpClient, converterFactory: Converter.Factory): Retrofit =
            Retrofit.Builder()
                .baseUrl(config.baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(converterFactory)
                .client(okHttpClient)
                .build()
    }
}