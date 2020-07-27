package org.stepic.droid.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.concurrency.MainHandlerImpl
import org.stepic.droid.configuration.Config
import org.stepic.droid.configuration.ConfigImpl
import org.stepic.droid.core.DefaultFilter
import org.stepic.droid.core.DefaultFilterImpl
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.ScreenManagerImpl
import org.stepik.android.remote.base.UserAgentProvider
import org.stepik.android.remote.base.UserAgentProviderImpl
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

@Module
abstract class ConfigModule {
    @Binds
    @AppSingleton
    internal abstract fun provideDefaultFilter(defaultFilter: DefaultFilterImpl): DefaultFilter
    @Binds
    @AppSingleton
    internal abstract fun provideUserAgent(userAgentProvider: UserAgentProviderImpl): UserAgentProvider
    @Binds
    @AppSingleton
    internal abstract fun provideScreenManager(screenManager: ScreenManagerImpl): ScreenManager
    @Module
    companion object {
        //it is good for many short lived, which should do async
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideThreadPool(): ThreadPoolExecutor {
            return Executors.newCachedThreadPool() as ThreadPoolExecutor
        }
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideConfig(configFactory: ConfigImpl.ConfigFactory): Config {
            return configFactory.create()
        }
    }
}