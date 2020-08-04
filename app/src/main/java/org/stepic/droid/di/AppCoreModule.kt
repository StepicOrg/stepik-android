package org.stepic.droid.di

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.net.ConnectivityManager
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.stepic.droid.BuildConfig
import org.stepic.droid.R
import org.stepic.droid.analytic.Analytic
import org.stepic.droid.base.Client
import org.stepic.droid.base.ClientImpl
import org.stepic.droid.base.ListenerContainer
import org.stepic.droid.base.ListenerContainerImpl
import org.stepic.droid.concurrency.MainHandler
import org.stepic.droid.concurrency.MainHandlerImpl
import org.stepic.droid.configuration.Config
import org.stepic.droid.configuration.ConfigImpl
import org.stepic.droid.core.DefaultFilter
import org.stepic.droid.core.DefaultFilterImpl
import org.stepic.droid.core.LessonSessionManager
import org.stepic.droid.core.LocalLessonSessionManagerImpl
import org.stepic.droid.core.ScreenManager
import org.stepic.droid.core.ScreenManagerImpl
import org.stepic.droid.core.ShareHelper
import org.stepic.droid.core.ShareHelperImpl
import org.stepic.droid.core.StepikDevicePoster
import org.stepic.droid.core.StepikDevicePosterImpl
import org.stepic.droid.core.internetstate.InternetEnabledPosterImpl
import org.stepic.droid.core.internetstate.contract.InternetEnabledListener
import org.stepic.droid.core.internetstate.contract.InternetEnabledPoster
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.notifications.BlockNotificationIntervalProvider
import org.stepic.droid.notifications.NotificationTimeChecker
import org.stepic.droid.notifications.NotificationTimeCheckerImpl
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.resolvers.StepTypeResolver
import org.stepic.droid.util.resolvers.StepTypeResolverImpl
import org.stepic.droid.util.resolvers.text.TextResolver
import org.stepic.droid.util.resolvers.text.TextResolverImpl
import org.stepik.android.presentation.base.injection.DaggerViewModelFactory
import org.stepik.android.remote.base.UserAgentProvider
import org.stepik.android.remote.base.UserAgentProviderImpl
import org.stepik.android.view.injection.billing.PublicLicenseKey
import org.stepik.android.view.injection.qualifiers.AuthLock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.locks.ReentrantReadWriteLock


@Module
abstract class AppCoreModule {

    @Binds
    @AppSingleton
    abstract fun bindStepikDevicePoster(stepikDevicePosterImpl: StepikDevicePosterImpl): StepikDevicePoster

    @Binds
    @AppSingleton
    abstract fun provideInternetEnabledPoster(internetEnabledPoster: InternetEnabledPosterImpl): InternetEnabledPoster

    @Binds
    @AppSingleton
    abstract fun provideInternetEnabledListenerContainer(container: ListenerContainerImpl<InternetEnabledListener>): ListenerContainer<InternetEnabledListener>

    @Binds
    @AppSingleton
    abstract fun provideInternetEnabledClient(container: ClientImpl<InternetEnabledListener>): Client<InternetEnabledListener>

    @AppSingleton
    @Binds
    internal abstract fun provideLessonSessionManager(localLessonSessionManager: LocalLessonSessionManagerImpl): LessonSessionManager

    @AppSingleton
    @Binds
    internal abstract fun provideHandlerForUIThread(mainHandler: MainHandlerImpl): MainHandler

    @Binds
    @AppSingleton
    internal abstract fun provideShareHelper(shareHelper: ShareHelperImpl): ShareHelper

    @Binds
    @AppSingleton
    internal abstract fun provideTextResolver(textResolver: TextResolverImpl): TextResolver

    @Binds
    @AppSingleton
    abstract fun bindStepTypeResolver(stepTypeResolver: StepTypeResolverImpl): StepTypeResolver

    @Binds
    internal abstract fun bindViewModelFactory(daggerViewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory

    @Module
    companion object {
        @Provides
        @JvmStatic
        @MainScheduler
        internal fun provideAndroidScheduler(): Scheduler = AndroidSchedulers.mainThread()

        @Provides
        @JvmStatic
        @BackgroundScheduler
        internal fun provideBackgroundScheduler(): Scheduler = Schedulers.io()

        @Provides
        @JvmStatic
        @AppSingleton
        internal fun provideSharedPreferencesHelper(analytic: Analytic, defaultFilter: DefaultFilter, context: Context, @AuthLock authLock: ReentrantReadWriteLock): SharedPreferenceHelper {
            return SharedPreferenceHelper(analytic, defaultFilter, context, authLock)
        }

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideUserPrefs(helper: SharedPreferenceHelper, analytic: Analytic): UserPreferences {
            return UserPreferences(helper, analytic)
        }

        @Provides
        @JvmStatic
        internal fun provideSystemAlarmManager(context: Context): AlarmManager {
            return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }

        /**
         * this retrofit is only for parsing error body
         */
        @Provides
        @AppSingleton
        @JvmStatic
        fun provideRetrofit(config: Config): Retrofit {
            return Retrofit.Builder()
                    .baseUrl(config.baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(OkHttpClient())
                    .build()
        }

        @Provides
        @JvmStatic
        @AppSingleton
        internal fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
            val firebaseRemoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                if (BuildConfig.DEBUG) {
                    minimumFetchIntervalInSeconds = 0
                }
            }
            firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
            firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            return firebaseRemoteConfig
        }


        @Provides
        @JvmStatic
        internal fun provideSystemNotificationManager(context: Context) =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        @Provides
        @JvmStatic
        internal fun provideRescheduleChecker(blockNotificationIntervalProvider: BlockNotificationIntervalProvider): NotificationTimeChecker {
            return NotificationTimeCheckerImpl(blockNotificationIntervalProvider.start, blockNotificationIntervalProvider.end)
        }


        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideBlockNotificationIntervalProvider() = BlockNotificationIntervalProvider()


        @Provides
        @JvmStatic
        internal fun provideConnectivityManager(context: Context): ConnectivityManager {
            return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        }

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideContentResolver(context: Context): ContentResolver =
            context.contentResolver

        @Provides
        @AppSingleton
        @JvmStatic
        @PublicLicenseKey
        internal fun providePublicLicenseKey(config: Config): String =
            config.appPublicLicenseKey
    }

}
