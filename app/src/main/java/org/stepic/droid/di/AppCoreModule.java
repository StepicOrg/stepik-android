package org.stepic.droid.di;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.content.Context;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.squareup.otto.Bus;

import org.stepic.droid.BuildConfig;
import org.stepic.droid.R;
import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.analytic.AnalyticImpl;
import org.stepic.droid.concurrency.MainHandler;
import org.stepic.droid.concurrency.MainHandlerImpl;
import org.stepic.droid.concurrency.SingleThreadExecutor;
import org.stepic.droid.configuration.Config;
import org.stepic.droid.configuration.ConfigReleaseImpl;
import org.stepic.droid.core.AudioFocusHelper;
import org.stepic.droid.core.CommentManager;
import org.stepic.droid.core.DefaultFilter;
import org.stepic.droid.core.DefaultFilterImpl;
import org.stepic.droid.core.FilterApplicator;
import org.stepic.droid.core.FilterApplicatorImpl;
import org.stepic.droid.core.LessonSessionManager;
import org.stepic.droid.core.LocalLessonSessionManagerImpl;
import org.stepic.droid.core.LocalProgressImpl;
import org.stepic.droid.core.LocalProgressManager;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.ScreenManagerImpl;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.core.ShareHelperImpl;
import org.stepic.droid.core.Shell;
import org.stepic.droid.core.ShellImpl;
import org.stepic.droid.core.StepikLogoutManager;
import org.stepic.droid.core.VideoLengthResolver;
import org.stepic.droid.core.VideoLengthResolverImpl;
import org.stepic.droid.fonts.FontsProvider;
import org.stepic.droid.fonts.FontsProviderImpl;
import org.stepic.droid.notifications.LocalReminder;
import org.stepic.droid.notifications.LocalReminderImpl;
import org.stepic.droid.notifications.NotificationManager;
import org.stepic.droid.notifications.NotificationManagerImpl;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.storage.CancelSniffer;
import org.stepic.droid.storage.CleanManager;
import org.stepic.droid.storage.CleanManagerImpl;
import org.stepic.droid.storage.ConcurrentCancelSniffer;
import org.stepic.droid.storage.DownloadManagerImpl;
import org.stepic.droid.storage.IDownloadManager;
import org.stepic.droid.storage.InitialDownloadUpdater;
import org.stepic.droid.storage.LessonDownloader;
import org.stepic.droid.storage.LessonDownloaderImpl;
import org.stepic.droid.storage.SectionDownloader;
import org.stepic.droid.storage.SectionDownloaderImpl;
import org.stepic.droid.storage.StoreStateManager;
import org.stepic.droid.storage.StoreStateManagerImpl;
import org.stepic.droid.storage.operations.DatabaseFacade;
import org.stepic.droid.util.resolvers.VideoResolver;
import org.stepic.droid.util.resolvers.VideoResolverImpl;
import org.stepic.droid.util.resolvers.text.TextResolver;
import org.stepic.droid.util.resolvers.text.TextResolverImpl;
import org.stepic.droid.web.Api;
import org.stepic.droid.web.ApiImpl;
import org.stepic.droid.web.UserAgentProvider;
import org.stepic.droid.web.UserAgentProviderImpl;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


@Module
public abstract class AppCoreModule {

    @Binds
    @AppSingleton
    abstract ScreenManager provideScreenManager(ScreenManagerImpl screenManager);

    @Binds
    @AppSingleton
    abstract Shell provideIShell(ShellImpl shell);

    @Binds
    @AppSingleton
    abstract Config provideIConfig(ConfigReleaseImpl configRelease);

    @Binds
    @AppSingleton
    abstract Api provideIApi(ApiImpl api);

    @Provides
    @AppSingleton
    static SharedPreferenceHelper provideSharedPreferencesHelper(Analytic analytic, DefaultFilter defaultFilter, Context context) {
        return new SharedPreferenceHelper(analytic, defaultFilter, context);
    }

    @Provides
    @AppSingleton
    static Bus provideBus() {
        return new Bus();
    }

    @Binds
    @AppSingleton
    abstract VideoResolver provideVideoResolver(VideoResolverImpl videoResolver);

    @Provides
    @AppSingleton
    static UserPreferences provideUserPrefs(Context context, SharedPreferenceHelper helper, Analytic analytic) {
        return new UserPreferences(context, helper, analytic);
    }

    @Binds
    @AppSingleton
    abstract IDownloadManager provideDownloadManger(DownloadManagerImpl downloadManager);

    @Provides
    static DownloadManager provideSystemDownloadManager(Context context) {
        return (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Provides
    static AlarmManager provideSystemAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @AppSingleton
    @Binds
    abstract StoreStateManager provideStoreManager(StoreStateManagerImpl storeStateManager);

    @AppSingleton
    @Binds
    abstract CleanManager provideCleanManager(CleanManagerImpl cleanManager);

    @AppSingleton
    @Provides
    static SocialManager provideSocialManager() {
        return new SocialManager();
    }

    @AppSingleton
    @Binds
    abstract LessonSessionManager provideLessonSessionManager(LocalLessonSessionManagerImpl localLessonSessionManager);

    @AppSingleton
    @Binds
    abstract LocalProgressManager provideProgressManager(LocalProgressImpl localProgress);

    @Binds
    @AppSingleton
    abstract CancelSniffer provideCancelSniffer(ConcurrentCancelSniffer cancelSniffer);

    @Provides
    @AppSingleton
    static SingleThreadExecutor provideSingle() {
        return new SingleThreadExecutor(Executors.newSingleThreadExecutor());
    }

    //it is good for many short lived, which should do async
    @Provides
    @AppSingleton
    static ThreadPoolExecutor provideThreadPool() {
        return (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    @AppSingleton
    @Binds
    abstract MainHandler provideHandlerForUIThread(MainHandlerImpl mainHandler);

    @AppSingleton
    @Provides
    static AudioFocusHelper provideAudioFocusHelper(Context context, MainHandler mainHandler, Bus bus) {
        return new AudioFocusHelper(context, bus, mainHandler);
    }

    @AppSingleton
    @Binds
    abstract LocalReminder provideLocalReminder(LocalReminderImpl localReminder);

    @AppSingleton
    @Binds
    abstract NotificationManager provideNotificationManager(NotificationManagerImpl notificationManager);

    @Provides
    static CommentManager provideCommentsManager() {
        return new CommentManager();
    }

    @Binds
    @AppSingleton
    abstract Analytic provideAnalytic(AnalyticImpl analytic);

    @Binds
    @AppSingleton
    abstract ShareHelper provideShareHelper(ShareHelperImpl shareHelper);

    @Binds
    @AppSingleton
    abstract DefaultFilter provideDefaultFilter(DefaultFilterImpl defaultFilter);

    @Binds
    @AppSingleton
    abstract FilterApplicator provideFilterApplicator(FilterApplicatorImpl filterApplicator);

    @Binds
    @AppSingleton
    abstract TextResolver provideTextResolver(TextResolverImpl textResolver);

    /**
     * this retrofit is only for parsing error body
     */
    @Provides
    @AppSingleton
    static Retrofit provideRetrofit(Config config) {
        return new Retrofit.Builder()
                .baseUrl(config.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient())
                .build();
    }

    @Provides
    @AppSingleton
    static StepikLogoutManager provideStepikLogoutManager(ThreadPoolExecutor threadPoolExecutor, MainHandler mainHandler, UserPreferences userPreferences, SharedPreferenceHelper sharedPreferenceHelper, DownloadManager downloadManager, DatabaseFacade dbFacade) {
        return new StepikLogoutManager(threadPoolExecutor, mainHandler, userPreferences, downloadManager, sharedPreferenceHelper, dbFacade);
    }

    @Provides
    @AppSingleton
    static InitialDownloadUpdater provideDownloadUpdaterAfterRestart(ThreadPoolExecutor threadPoolExecutor,
                                                              DownloadManager systemDownloadManager,
                                                              StoreStateManager storeStateManager,
                                                              DatabaseFacade databaseFacade) {
        return new InitialDownloadUpdater(threadPoolExecutor, systemDownloadManager, storeStateManager, databaseFacade);
    }

    @Binds
    @AppSingleton
    abstract LessonDownloader provideLessonDownloader(LessonDownloaderImpl lessonDownloader);

    @Binds
    @AppSingleton
    abstract SectionDownloader provideSectionDownloader(SectionDownloaderImpl sectionDownloader);

    @Binds
    @AppSingleton
    abstract VideoLengthResolver provideVideoLengthResolver(VideoLengthResolverImpl videoLengthResolver);

    @Binds
    @AppSingleton
    abstract UserAgentProvider provideUserAgent(UserAgentProviderImpl userAgentProvider);

    @Provides
    @AppSingleton
    static FirebaseRemoteConfig provideFirebaseRemoteConfig() {
        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        return firebaseRemoteConfig;
    }

    @Binds
    @AppSingleton
    abstract FontsProvider provideFontProvider(FontsProviderImpl fontsProvider);

}
