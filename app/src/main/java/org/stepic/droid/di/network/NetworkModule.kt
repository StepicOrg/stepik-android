package org.stepic.droid.di.network

import android.webkit.CookieManager
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.stepic.droid.configuration.Config
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.util.DebugToolsHelper
import org.stepic.droid.web.NetworkFactory
import org.stepik.android.view.injection.achievement.AchievementDataModule
import org.stepik.android.view.injection.auth.AuthModule
import org.stepik.android.view.injection.base.Authorized
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit

@Module(includes = [AuthModule::class, SerializationModule::class, AchievementDataModule::class])
abstract class NetworkModule {

    @Module
    companion object {
        const val TIMEOUT_IN_SECONDS = 60L

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
        internal fun provideOkHttpClient(interceptors: Set<@JvmSuppressWildcards Interceptor>): OkHttpClient {
            val okHttpBuilder = OkHttpClient.Builder()
                .connectTimeout(NetworkFactory.TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                .readTimeout(NetworkFactory.TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
            interceptors.forEach { okHttpBuilder.addNetworkInterceptor(it) }

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