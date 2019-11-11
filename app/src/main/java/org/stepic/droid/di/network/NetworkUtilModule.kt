package org.stepic.droid.di.network

import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import org.stepic.droid.di.AppSingleton
import org.stepic.droid.util.DebugToolsHelper
import org.stepik.android.view.injection.qualifiers.DebugInterceptors

@Module
abstract class NetworkUtilModule {

    @Module
    companion object {
        @AppSingleton
        @Provides
        @JvmStatic
        @DebugInterceptors
        fun provideStethoInterceptor(): List<Interceptor> =
            DebugToolsHelper.getDebugInterceptors()
    }
}