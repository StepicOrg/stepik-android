package org.stepic.droid.core;

import android.app.DownloadManager;
import android.content.Context;

import com.squareup.otto.Bus;

import org.stepic.droid.configuration.ConfigRelease;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.DownloadManagerImpl;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.StoreStateManager;
import org.stepic.droid.store.operations.DatabaseManager;
import org.stepic.droid.util.resolvers.CoursePropertyResolver;
import org.stepic.droid.util.resolvers.ISearchResolver;
import org.stepic.droid.util.resolvers.IStepResolver;
import org.stepic.droid.util.resolvers.IVideoResolver;
import org.stepic.droid.util.resolvers.SearchResolver;
import org.stepic.droid.util.resolvers.StepTypeResolver;
import org.stepic.droid.util.resolvers.VideoResolver;
import org.stepic.droid.web.HttpManager;
import org.stepic.droid.web.IApi;
import org.stepic.droid.web.IHttpManager;
import org.stepic.droid.web.RetrofitRESTApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class StepicDefaultModule {

    private final Context mContext;

    public StepicDefaultModule(Context context) {
        this.mContext = context;
    }

    @Provides
    @Singleton
    public IScreenManager provideIScreenManager(IConfig config) {
        return new ScreenManager(config);
    }

    @Provides
    @Singleton
    public IShell provideIShell(Context context) {
        return new Shell(context);
    }

    @Provides
    @Singleton
    public IConfig provideIConfig(Context context) {
        return new ConfigRelease(context);
    }


    @Provides
    @Singleton
    public IApi provideIApi() {
        return new RetrofitRESTApi();
    }

    @Provides
    @Singleton
    public IHttpManager provideIHttpManager(Context context) {
        return new HttpManager(context);
    }

    @Provides
    @Singleton
    public SharedPreferenceHelper provideSharedPreferencesHelper() {
        return new SharedPreferenceHelper();
    }

    @Provides
    @Singleton
    public Context provideApplicationContext() {
        return mContext;
    }

    @Provides
    @Singleton
    public Bus provideBus() {
        return new Bus();
    }

    @Provides
    @Singleton
    public IStepResolver provideStepResolver(Context context) {
        return new StepTypeResolver(context);
    }

    @Provides
    @Singleton
    public IVideoResolver provideVideoResolver(Context context,
                                               Bus bus,
                                               DatabaseManager dbOperationsCachedVideo,
                                               UserPreferences userPreferences) {
        return new VideoResolver(context, bus, dbOperationsCachedVideo, userPreferences);
    }

    @Provides
    @Singleton
    public UserPreferences provideUserPrefs(Context context, SharedPreferenceHelper helper) {
        return new UserPreferences(context, helper);
    }

    @Provides
    @Singleton
    public IDownloadManager provideDownloadManger() {
        return new DownloadManagerImpl();
    }

    @Provides
    public DownloadManager provideSystemDownloadManager(Context context) {
        return (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Singleton
    @Provides
    public DatabaseManager provideDbOperationCachedVideo(Context context) {
        return new DatabaseManager(context);
    }

    @Singleton
    @Provides
    public IStoreStateManager provideStoreManager(DatabaseManager dbManager, Bus bus) {
        return new StoreStateManager(dbManager, bus);
    }

    @Singleton
    @Provides
    public CleanManager provideCleanManager() {
        return new CleanManager();
    }

    @Singleton
    @Provides
    public SocialManager provideSocialManager() {
        return new SocialManager();
    }

    @Singleton
    @Provides
    public CoursePropertyResolver provideCoursePropertyResolver() {
        return new CoursePropertyResolver();
    }

    @Singleton
    @Provides
    public ISearchResolver provideSearchResolver() {
        return new SearchResolver();
    }

    @Singleton
    @Provides
    public ILessonSessionManager provideLessonSessionManager() {
        return new LocalLessonSessionManager();
    }

    @Singleton
    @Provides
    public ILocalProgressManager provideProgressManager(DatabaseManager databaseManager, Bus bus) {
        return new LocalProgressOfUnitManager(databaseManager, bus);
    }
}
