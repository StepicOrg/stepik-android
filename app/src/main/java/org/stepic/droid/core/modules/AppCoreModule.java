package org.stepic.droid.core.modules;

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
import org.stepic.droid.store.CancelSniffer;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.CleanManagerImpl;
import org.stepic.droid.store.ConcurrentCancelSniffer;
import org.stepic.droid.store.DownloadManagerImpl;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.InitialDownloadUpdater;
import org.stepic.droid.store.LessonDownloader;
import org.stepic.droid.store.LessonDownloaderImpl;
import org.stepic.droid.store.SectionDownloader;
import org.stepic.droid.store.SectionDownloaderImpl;
import org.stepic.droid.store.StoreStateManager;
import org.stepic.droid.store.StoreStateManagerImpl;
import org.stepic.droid.store.operations.DatabaseFacade;
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

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


@Module(includes = {StorageModule.class})
public abstract class AppCoreModule {

    @Binds
    @Singleton
    abstract ScreenManager provideScreenManager(ScreenManagerImpl screenManager);

    @Binds
    @Singleton
    abstract Shell provideIShell(ShellImpl shell);

    @Binds
    @Singleton
    abstract Config provideIConfig(ConfigReleaseImpl configRelease);

    @Binds
    @Singleton
    abstract Api provideIApi(ApiImpl api);

    @Provides
    @Singleton
    static SharedPreferenceHelper provideSharedPreferencesHelper(Analytic analytic, DefaultFilter defaultFilter, Context context) {
        return new SharedPreferenceHelper(analytic, defaultFilter, context);
    }

    @Provides
    @Singleton
    static Bus provideBus() {
        return new Bus();
    }

    @Binds
    @Singleton
    abstract VideoResolver provideVideoResolver(VideoResolverImpl videoResolver);

    @Provides
    @Singleton
    static UserPreferences provideUserPrefs(Context context, SharedPreferenceHelper helper, Analytic analytic) {
        return new UserPreferences(context, helper, analytic);
    }

    @Binds
    @Singleton
    abstract IDownloadManager provideDownloadManger(DownloadManagerImpl downloadManager);

    @Provides
    static DownloadManager provideSystemDownloadManager(Context context) {
        return (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Provides
    static AlarmManager provideSystemAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Singleton
    @Binds
    abstract StoreStateManager provideStoreManager(StoreStateManagerImpl storeStateManager);

    @Singleton
    @Binds
    abstract CleanManager provideCleanManager(CleanManagerImpl cleanManager);

    @Singleton
    @Provides
    static SocialManager provideSocialManager() {
        return new SocialManager();
    }

    @Singleton
    @Binds
    abstract LessonSessionManager provideLessonSessionManager(LocalLessonSessionManagerImpl localLessonSessionManager);

    @Singleton
    @Binds
    abstract LocalProgressManager provideProgressManager(LocalProgressImpl localProgress);

    @Binds
    @Singleton
    abstract CancelSniffer provideCancelSniffer(ConcurrentCancelSniffer cancelSniffer);

    @Provides
    @Singleton
    static SingleThreadExecutor provideSingle() {
        return new SingleThreadExecutor(Executors.newSingleThreadExecutor());
    }

    //it is good for many short lived, which should do async
    @Provides
    @Singleton
    static ThreadPoolExecutor provideThreadPool() {
        return (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    @Singleton
    @Binds
    abstract MainHandler provideHandlerForUIThread(MainHandlerImpl mainHandler);

    @Singleton
    @Provides
    static AudioFocusHelper provideAudioFocusHelper(Context context, MainHandler mainHandler, Bus bus) {
        return new AudioFocusHelper(context, bus, mainHandler);
    }

    @Singleton
    @Binds
    abstract LocalReminder provideLocalReminder(LocalReminderImpl localReminder);

    @Singleton
    @Binds
    abstract NotificationManager provideNotificationManager(NotificationManagerImpl notificationManager);

    @Provides
    static CommentManager provideCommentsManager() {
        return new CommentManager();
    }

    @Binds
    @Singleton
    abstract Analytic provideAnalytic(AnalyticImpl analytic);

    @Binds
    @Singleton
    abstract ShareHelper provideShareHelper(ShareHelperImpl shareHelper);

    @Binds
    @Singleton
    abstract DefaultFilter provideDefaultFilter(DefaultFilterImpl defaultFilter);

    @Binds
    @Singleton
    abstract FilterApplicator provideFilterApplicator(FilterApplicatorImpl filterApplicator);

    @Binds
    @Singleton
    abstract TextResolver provideTextResolver(TextResolverImpl textResolver);

    /**
     * this retrofit is only for parsing error body
     */
    @Provides
    @Singleton
    static Retrofit provideRetrofit(Config config) {
        return new Retrofit.Builder()
                .baseUrl(config.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient())
                .build();
    }

    @Provides
    @Singleton
    static StepikLogoutManager provideStepikLogoutManager(ThreadPoolExecutor threadPoolExecutor, MainHandler mainHandler, UserPreferences userPreferences, SharedPreferenceHelper sharedPreferenceHelper, DownloadManager downloadManager, DatabaseFacade dbFacade) {
        return new StepikLogoutManager(threadPoolExecutor, mainHandler, userPreferences, downloadManager, sharedPreferenceHelper, dbFacade);
    }

    @Provides
    @Singleton
    static InitialDownloadUpdater provideDownloadUpdaterAfterRestart(ThreadPoolExecutor threadPoolExecutor,
                                                              DownloadManager systemDownloadManager,
                                                              StoreStateManager storeStateManager,
                                                              DatabaseFacade databaseFacade) {
        return new InitialDownloadUpdater(threadPoolExecutor, systemDownloadManager, storeStateManager, databaseFacade);
    }

    @Binds
    @Singleton
    abstract LessonDownloader provideLessonDownloader(LessonDownloaderImpl lessonDownloader);

    @Binds
    @Singleton
    abstract SectionDownloader provideSectionDownloader(SectionDownloaderImpl sectionDownloader);

    @Binds
    @Singleton
    abstract VideoLengthResolver provideVideoLengthResolver(VideoLengthResolverImpl videoLengthResolver);

    @Binds
    @Singleton
    abstract UserAgentProvider provideUserAgent(UserAgentProviderImpl userAgentProvider);

    @Provides
    @Singleton
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
    @Singleton
    abstract FontsProvider provideFontProvider(FontsProviderImpl fontsProvider);

}
