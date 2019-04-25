package org.stepic.droid.di

import android.app.AlarmManager
import android.app.NotificationManager
import android.arch.lifecycle.ViewModelProvider
import android.content.ContentResolver
import android.content.Context
import android.net.ConnectivityManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
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
import org.stepic.droid.concurrency.SingleThreadExecutor
import org.stepic.droid.configuration.Config
import org.stepic.droid.configuration.ConfigImpl
import org.stepic.droid.core.*
import org.stepic.droid.core.internetstate.InternetEnabledPosterImpl
import org.stepic.droid.core.internetstate.contract.InternetEnabledListener
import org.stepic.droid.core.internetstate.contract.InternetEnabledPoster
import org.stepic.droid.di.qualifiers.BackgroundScheduler
import org.stepic.droid.di.qualifiers.MainScheduler
import org.stepic.droid.fonts.FontsProvider
import org.stepic.droid.fonts.FontsProviderImpl
import org.stepic.droid.notifications.*
import org.stepic.droid.preferences.SharedPreferenceHelper
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.social.SocialManager
import org.stepic.droid.util.resolvers.StepTypeResolver
import org.stepic.droid.util.resolvers.StepTypeResolverImpl
import org.stepic.droid.util.resolvers.VideoResolver
import org.stepic.droid.util.resolvers.VideoResolverImpl
import org.stepic.droid.util.resolvers.text.TextResolver
import org.stepic.droid.util.resolvers.text.TextResolverImpl
import org.stepic.droid.web.Api
import org.stepic.droid.web.ApiImpl
import org.stepic.droid.web.UserAgentProvider
import org.stepic.droid.web.UserAgentProviderImpl
import org.stepik.android.presentation.base.injection.DaggerViewModelFactory
import org.stepik.android.view.injection.course.CourseScope
import org.stepik.android.view.notification.*
import org.stepik.android.view.notification.delegate.TestDelegate
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import javax.inject.Named


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

    @Binds
    @AppSingleton
    internal abstract fun provideScreenManager(screenManager: ScreenManagerImpl): ScreenManager

    @Binds
    @AppSingleton
    internal abstract fun provideIApi(api: ApiImpl): Api

    @Binds
    @AppSingleton
    internal abstract fun provideVideoResolver(videoResolver: VideoResolverImpl): VideoResolver

    @AppSingleton
    @Binds
    internal abstract fun provideLessonSessionManager(localLessonSessionManager: LocalLessonSessionManagerImpl): LessonSessionManager

    @AppSingleton
    @Binds
    internal abstract fun provideHandlerForUIThread(mainHandler: MainHandlerImpl): MainHandler

    @AppSingleton
    @Binds
    internal abstract fun provideLocalReminder(localReminder: LocalReminderImpl): LocalReminder

    @AppSingleton
    @Binds
    internal abstract fun provideNotificationManager(notificationManager: StepikNotificationManagerImpl): StepikNotificationManager

    @Binds
    @AppSingleton
    internal abstract fun provideShareHelper(shareHelper: ShareHelperImpl): ShareHelper

    @Binds
    @AppSingleton
    internal abstract fun provideDefaultFilter(defaultFilter: DefaultFilterImpl): DefaultFilter

    @Binds
    @AppSingleton
    internal abstract fun provideFilterApplicator(filterApplicator: FilterApplicatorImpl): FilterApplicator

    @Binds
    @AppSingleton
    internal abstract fun provideTextResolver(textResolver: TextResolverImpl): TextResolver

    @Binds
    @AppSingleton
    internal abstract fun provideVideoLengthResolver(videoLengthResolver: VideoLengthResolverImpl): VideoLengthResolver

    @Binds
    @AppSingleton
    internal abstract fun provideUserAgent(userAgentProvider: UserAgentProviderImpl): UserAgentProvider

    @Binds
    @AppSingleton
    internal abstract fun provideFontProvider(fontsProvider: FontsProviderImpl): FontsProvider

    @Binds
    @AppSingleton
    abstract fun bindStepTypeResolver(stepTypeResolver: StepTypeResolverImpl): StepTypeResolver

    @Binds
    internal abstract fun bindViewModelFactory(daggerViewModelFactory: DaggerViewModelFactory): ViewModelProvider.Factory

    @Binds
    internal abstract fun bindNotificationPublisher(notificationPublisherImpl: NotificationPublisherImpl): NotificationPublisher

    @Binds
    internal abstract fun bindStepikNotifManager(stepikNotifManagerImpl: StepikNotifManagerImpl): StepikNotifManager

    @IntoSet
    @Binds
    internal abstract fun provideTestDelegate(testDelegate: TestDelegate): NotificationDelegate

    @Module
    companion object {
        const val SINGLE_THREAD_CODE_SAVER = "SINGLE_THREAD_CODE_SAVER"

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
        internal fun provideSharedPreferencesHelper(analytic: Analytic, defaultFilter: DefaultFilter, context: Context): SharedPreferenceHelper {
            return SharedPreferenceHelper(analytic, defaultFilter, context)
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

        @AppSingleton
        @Provides
        @JvmStatic
        internal fun provideSocialManager(): SocialManager {
            return SocialManager()
        }

        @AppSingleton
        @Provides
        @JvmStatic
        @Named(SINGLE_THREAD_CODE_SAVER)
        internal fun provideSingleThreadExecutorForCode(): SingleThreadExecutor =
                SingleThreadExecutor(Executors.newSingleThreadExecutor())

        //it is good for many short lived, which should do async
        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideThreadPool(): ThreadPoolExecutor {
            return Executors.newCachedThreadPool() as ThreadPoolExecutor
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
            val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build()
            firebaseRemoteConfig.setConfigSettings(configSettings)
            firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults)
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
        internal fun provideConfig(configFactory: ConfigImpl.ConfigFactory): Config {
            return configFactory.create()
        }

        @Provides
        @AppSingleton
        @JvmStatic
        internal fun provideContentResolver(context: Context): ContentResolver =
            context.contentResolver
    }

}
