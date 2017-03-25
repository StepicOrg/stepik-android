package org.stepic.droid.core.modules;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;

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
import org.stepic.droid.notifications.INotificationManager;
import org.stepic.droid.notifications.LocalReminder;
import org.stepic.droid.notifications.LocalReminderImpl;
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
import org.stepic.droid.util.resolvers.CoursePropertyResolver;
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

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


@Module(includes = {StorageModule.class})
public class AppCoreModule {

    @Provides
    @Singleton
    ScreenManager provideIScreenManager(Config config, UserPreferences userPreferences, Analytic analytic, SharedPreferenceHelper sharedPreferenceHelper) {
        return new ScreenManagerImpl(config, userPreferences, analytic, sharedPreferenceHelper);
    }

    @Provides
    @Singleton
    Shell provideIShell(Context context) {
        return new ShellImpl(context);
    }

    @Provides
    @Singleton
    Config provideIConfig(Context context, Analytic analytic) {
        return new ConfigReleaseImpl(context, analytic);
    }

    @Provides
    @Singleton
    Api provideIApi() {
        return new ApiImpl();
    }

    @Provides
    @Singleton
    SharedPreferenceHelper provideSharedPreferencesHelper(Analytic analytic, DefaultFilter defaultFilter, Context context) {
        return new SharedPreferenceHelper(analytic, defaultFilter, context);
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }

    @Provides
    @Singleton
    VideoResolver provideVideoResolver(Analytic analytic,
                                       DatabaseFacade dbOperationsCachedVideo,
                                       UserPreferences userPreferences,
                                       CleanManager cleanManager) {
        return new VideoResolverImpl(dbOperationsCachedVideo, userPreferences, cleanManager, analytic);
    }

    @Provides
    @Singleton
    UserPreferences provideUserPrefs(Context context, SharedPreferenceHelper helper, Analytic analytic) {
        return new UserPreferences(context, helper, analytic);
    }

    @Provides
    @Singleton
    IDownloadManager provideDownloadManger() {
        return new DownloadManagerImpl();
    }

    @Provides
    DownloadManager provideSystemDownloadManager(Context context) {
        return (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Provides
    AlarmManager provideSystemAlarmManager(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Singleton
    @Provides
    StoreStateManager provideStoreManager(DatabaseFacade databaseFacade, Bus bus, Analytic analytic, MainHandler mainHandler) {
        return new StoreStateManagerImpl(databaseFacade, bus, analytic, mainHandler);
    }

    @Singleton
    @Provides
    CleanManager provideCleanManager(Context context) {
        return new CleanManagerImpl(context);
    }

    @Singleton
    @Provides
    SocialManager provideSocialManager() {
        return new SocialManager();
    }

    @Singleton
    @Provides
    CoursePropertyResolver provideCoursePropertyResolver() {
        return new CoursePropertyResolver();
    }

    @Singleton
    @Provides
    LessonSessionManager provideLessonSessionManager() {
        return new LocalLessonSessionManagerImpl();
    }

    @Singleton
    @Provides
    LocalProgressManager provideProgressManager(DatabaseFacade databaseFacade, Bus bus, Api api, MainHandler mainHandler) {
        return new LocalProgressImpl(databaseFacade, bus, api, mainHandler);
    }

    @Provides
    @Singleton
    CancelSniffer provideCancelSniffer() {
        return new ConcurrentCancelSniffer();
    }

    @Provides
    @Singleton
    SingleThreadExecutor provideSingle() {
        return new SingleThreadExecutor(Executors.newSingleThreadExecutor());
    }


    //it is good for many short lived, which should do async
    @Provides
    @Singleton
    ThreadPoolExecutor provideThreadPool() {
        return (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    @Singleton
    @Provides
    MainHandler provideHandlerForUIThread() {
        return new MainHandlerImpl();
    }

    @Singleton
    @Provides
    AudioFocusHelper provideAudioFocusHelper(Context context, MainHandler mainHandler, Bus bus) {
        return new AudioFocusHelper(context, bus, mainHandler);
    }

    @Singleton
    @Provides
    LocalReminder provideLocalReminder(SharedPreferenceHelper sp,
                                       DatabaseFacade db,
                                       Analytic analytic,
                                       ThreadPoolExecutor threadPoolExecutor,
                                       Context context,
                                       AlarmManager alarmManager) {
        return new LocalReminderImpl(threadPoolExecutor,
                sp,
                db,
                context,
                alarmManager,
                analytic);
    }

    @Singleton
    @Provides
    INotificationManager provideNotificationManager(SharedPreferenceHelper sp,
                                                    Api api,
                                                    Config config,
                                                    UserPreferences userPreferences,
                                                    DatabaseFacade db, Analytic analytic,
                                                    TextResolver textResolver,
                                                    ScreenManagerImpl screenManager,
                                                    ThreadPoolExecutor threadPoolExecutor,
                                                    Context context,
                                                    LocalReminder localReminder) {
        return new NotificationManagerImpl(sp, api, config, userPreferences, db, analytic, textResolver, screenManager, threadPoolExecutor, context, localReminder);
    }

    @Provides
    CommentManager provideCommentsManager() {
        return new CommentManager();
    }

    @Provides
    @Singleton
    Analytic provideAnalytic(Context context) {
        return new AnalyticImpl(context);
    }

    @Provides
    @Singleton
    ShareHelper provideShareHelper(Config config, Context context, TextResolver textResolver) {
        return new ShareHelperImpl(config, context, textResolver);
    }

    @Provides
    @Singleton
    DefaultFilter provideDefaultFilter(Context context) {
        return new DefaultFilterImpl(context);
    }

    @Provides
    @Singleton
    FilterApplicator provideFilterApplicator(DefaultFilter defaultFilter, SharedPreferenceHelper sharedPreferenceHelper) {
        return new FilterApplicatorImpl(defaultFilter, sharedPreferenceHelper);
    }

    @Provides
    @Singleton
    TextResolver provideTextResolver(Config config) {
        return new TextResolverImpl(config);
    }


    /**
     * it is workaround for provide view Single pool to notification
     */
    @Provides
    @Singleton
    RecyclerView.RecycledViewPool provideRecycledViewPool() {
        return new RecyclerView.RecycledViewPool();
    }

    /**
     * this retrofit is only for parsing error body
     */
    @Provides
    @Singleton
    Retrofit provideRetrofit(Config config) {
        return new Retrofit.Builder()
                .baseUrl(config.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient())
                .build();
    }

    @Provides
    @Singleton
    StepikLogoutManager provideStepikLogoutManager(ThreadPoolExecutor threadPoolExecutor, MainHandler mainHandler, UserPreferences userPreferences, SharedPreferenceHelper sharedPreferenceHelper, DownloadManager downloadManager, DatabaseFacade dbFacade) {
        return new StepikLogoutManager(threadPoolExecutor, mainHandler, userPreferences, downloadManager, sharedPreferenceHelper, dbFacade);
    }

    @Provides
    @Singleton
    InitialDownloadUpdater provideDownloadUpdaterAfterRestart(ThreadPoolExecutor threadPoolExecutor,
                                                              DownloadManager systemDownloadManager,
                                                              StoreStateManager storeStateManager,
                                                              DatabaseFacade databaseFacade) {
        return new InitialDownloadUpdater(threadPoolExecutor, systemDownloadManager, storeStateManager, databaseFacade);
    }

    @Provides
    @Singleton
    LessonDownloader provideLessonDownloader(
            ThreadPoolExecutor threadPoolExecutor,
            DatabaseFacade databaseFacade,
            IDownloadManager downloadManager,
            CancelSniffer cancelSniffer,
            CleanManager cleanManager
    ) {
        return new LessonDownloaderImpl(databaseFacade, downloadManager, threadPoolExecutor, cleanManager, cancelSniffer);
    }


    @Provides
    @Singleton
    SectionDownloader provideSectionDownloader(
            ThreadPoolExecutor threadPoolExecutor,
            DatabaseFacade databaseFacade,
            IDownloadManager downloadManager,
            CancelSniffer cancelSniffer,
            CleanManager cleanManager
    ) {
        return new SectionDownloaderImpl(databaseFacade, downloadManager, threadPoolExecutor, cleanManager, cancelSniffer);
    }

    @Provides
    @Singleton
    VideoLengthResolver provideVideoLengthResolver() {
        return new VideoLengthResolverImpl();
    }

    @Provides
    @Singleton
    UserAgentProvider provideUserAgent(Context context) {
        return new UserAgentProviderImpl(context);
    }


    @Provides
    @Singleton
    FirebaseRemoteConfig provideFirebaseRemoteConfig() {
        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        return firebaseRemoteConfig;
    }

    @Provides
    @Singleton
    FontsProvider provideFontProvider() {
        return new FontsProviderImpl();
    }

}
