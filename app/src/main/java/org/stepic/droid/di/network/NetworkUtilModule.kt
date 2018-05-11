package org.stepic.droid.di.network

import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import org.stepic.droid.BuildConfig
import org.stepic.droid.di.AppSingleton

@Module
abstract class NetworkUtilModule {

    @Module
    companion object {
        @AppSingleton
        @Provides
        @JvmStatic
        @StethoInterceptor
        fun provideStethoInterceptor(): Interceptor = if (BuildConfig.DEBUG) {
            com.facebook.stetho.okhttp3.StethoInterceptor()
        } else {
            Interceptor { it.proceed(it.request()) }
        }
    }
}