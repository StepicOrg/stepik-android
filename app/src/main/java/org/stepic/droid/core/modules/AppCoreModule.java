package org.stepic.droid.core.modules;

import android.app.DownloadManager;
import android.content.Context;

import com.squareup.otto.Bus;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.analytic.AnalyticImpl;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.concurrency.MainHandlerImpl;
import org.stepic.droid.configuration.ConfigRelease;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.core.AudioFocusHelper;
import org.stepic.droid.core.CommentManager;
import org.stepic.droid.core.DefaultFilter;
import org.stepic.droid.core.DefaultFilterImpl;
import org.stepic.droid.core.FilterApplicator;
import org.stepic.droid.core.FilterApplicatorImpl;
import org.stepic.droid.core.ILessonSessionManager;
import org.stepic.droid.core.ILoginManager;
import org.stepic.droid.core.IScreenManager;
import org.stepic.droid.core.IShell;
import org.stepic.droid.core.LocalLessonSessionManager;
import org.stepic.droid.core.LocalProgressImpl;
import org.stepic.droid.core.LocalProgressManager;
import org.stepic.droid.core.LoginManager;
import org.stepic.droid.core.ScreenManager;
import org.stepic.droid.core.ShareHelper;
import org.stepic.droid.core.ShareHelperImpl;
import org.stepic.droid.core.Shell;
import org.stepic.droid.notifications.INotificationManager;
import org.stepic.droid.notifications.NotificationManagerImpl;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.ConcurrentCancelSniffer;
import org.stepic.droid.store.DownloadManagerImpl;
import org.stepic.droid.store.ICancelSniffer;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.StoreStateManager;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.resolvers.CoursePropertyResolver;
import org.stepic.droid.util.resolvers.IVideoResolver;
import org.stepic.droid.util.resolvers.TextResolver;
import org.stepic.droid.util.resolvers.TextResolverImpl;
import org.stepic.droid.util.resolvers.VideoResolver;
import org.stepic.droid.web.IApi;
import org.stepic.droid.web.RetrofitRESTApi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module(includes = {StorageModule.class})
public class AppCoreModule {

    private final Context context;

    public AppCoreModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    IScreenManager provideIScreenManager(IConfig config, UserPreferences userPreferences, Analytic analytic) {
        return new ScreenManager(config, userPreferences, analytic);
    }

    @Provides
    @Singleton
    IShell provideIShell(Context context) {
        return new Shell(context);
    }

    @Provides
    @Singleton
    IConfig provideIConfig(Context context, Analytic analytic) {
        return new ConfigRelease(context, analytic);
    }

    @Provides
    @Singleton
    IApi provideIApi() {
        return new RetrofitRESTApi();
    }

    @Provides
    @Singleton
    SharedPreferenceHelper provideSharedPreferencesHelper(Analytic analytic, DefaultFilter defaultFilter, Context context) {
        return new SharedPreferenceHelper(analytic, defaultFilter, context);
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return context;
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus();
    }

    @Provides
    @Singleton
    IVideoResolver provideVideoResolver(Analytic analytic,
                                        DatabaseFacade dbOperationsCachedVideo,
                                        UserPreferences userPreferences,
                                        CleanManager cleanManager) {
        return new VideoResolver(dbOperationsCachedVideo, userPreferences, cleanManager, analytic);
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

    @Singleton
    @Provides
    IStoreStateManager provideStoreManager(DatabaseFacade dbManager, Bus bus, Analytic analytic) {
        return new StoreStateManager(dbManager, bus, analytic);
    }

    @Singleton
    @Provides
    CleanManager provideCleanManager() {
        return new CleanManager();
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
    ILessonSessionManager provideLessonSessionManager() {
        return new LocalLessonSessionManager();
    }

    @Singleton
    @Provides
    LocalProgressManager provideProgressManager(DatabaseFacade databaseFacade, Bus bus, IApi api) {
        return new LocalProgressImpl(databaseFacade, bus, api);
    }

    @Singleton
    @Provides
    ILoginManager provideLoginManager(IShell shell, Context context, Analytic analytic) {
        return new LoginManager(shell, context, analytic);
    }

    @Provides
    @Singleton
    ICancelSniffer provideCancelSniffer() {
        return new ConcurrentCancelSniffer();
    }

    @Provides
    @Singleton
    ExecutorService provideSingle() {
        return Executors.newSingleThreadExecutor();
    }


    //it is good for many short lived, which should do async
    @Provides
    @Singleton
    ThreadPoolExecutor provideThreadPool() {
        return (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    @Singleton
    @Provides
    IMainHandler provideHandlerForUIThread() {
        return new MainHandlerImpl();
    }

    @Singleton
    @Provides
    AudioFocusHelper provideAudioFocusHelper(Context context, IMainHandler mainHandler, Bus bus) {
        return new AudioFocusHelper(context, bus, mainHandler);
    }

    @Singleton
    @Provides
    INotificationManager provideNotificationManager(SharedPreferenceHelper sp, IApi api, IConfig config, UserPreferences userPreferences, DatabaseFacade db, Analytic analytic) {
        return new NotificationManagerImpl(sp, api, config, userPreferences, db, analytic);
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
    ShareHelper provideShareHelper(IConfig config, Context context) {
        return new ShareHelperImpl(config, context);
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
    TextResolver provideTextResolver() {
        return new TextResolverImpl();
    }
}
