package org.stepic.droid.core;

import android.app.DownloadManager;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.squareup.otto.Bus;

import org.stepic.droid.configuration.ConfigRelease;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.model.Assignment;
import org.stepic.droid.model.BlockPersistentWrapper;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.DatabaseHelper;
import org.stepic.droid.store.DownloadManagerImpl;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.StoreStateManager;
import org.stepic.droid.store.dao.AssignmentDaoImpl;
import org.stepic.droid.store.dao.BlockDaoImpl;
import org.stepic.droid.store.dao.DownloadEntityDaoImpl;
import org.stepic.droid.store.dao.IDao;
import org.stepic.droid.store.dao.LessonDaoImpl;
import org.stepic.droid.store.dao.PersistentVideoDaoImpl;
import org.stepic.droid.store.dao.ProgressDaoImpl;
import org.stepic.droid.store.dao.SectionDaoImpl;
import org.stepic.droid.store.dao.StepDaoImpl;
import org.stepic.droid.store.dao.UnitDaoImpl;
import org.stepic.droid.store.dao.ViewAssignmentDaoImpl;
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
import org.stepic.droid.web.ViewAssignment;

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
    public ILocalProgressManager provideProgressManager(DatabaseManager databaseManager, Bus bus, IApi api) {
        return new LocalProgressOfUnitManager(databaseManager, bus, api);
    }

    @Singleton
    @Provides
    public ILoginManager provideLoginManager(IShell shell, Context context) {
        return new LoginManager(shell, context);
    }

    @Singleton
    @Provides
    public SQLiteOpenHelper provideSqlOpenHelper(Context context) {
        return new DatabaseHelper(context);
    }

    @Provides
    public IDao<Section> provideSectionDao(SQLiteOpenHelper openHelper){
        return new SectionDaoImpl(openHelper);
    }

    @Provides
    public IDao<Unit> provideUnitDao(SQLiteOpenHelper openHelper, IDao<Progress> progressDao){
        return new UnitDaoImpl(openHelper, progressDao);
    }

    @Provides
    public IDao<Progress> provideProgressDao(SQLiteOpenHelper openHelper) {
        return new ProgressDaoImpl(openHelper);
    }

    @Provides
    public IDao<Assignment> provideAssignmentDao(SQLiteOpenHelper openHelper) {
        return new AssignmentDaoImpl(openHelper);
    }

    @Provides
    public IDao<Lesson> provideLessonDao(SQLiteOpenHelper openHelper) {
        return new LessonDaoImpl(openHelper);
    }

    @Provides
    public IDao<ViewAssignment> provideViewAssignment(SQLiteOpenHelper openHelper) {
        return new ViewAssignmentDaoImpl(openHelper);
    }

    @Provides
    public IDao<DownloadEntity> provideDownloadEntity(SQLiteOpenHelper openHelper) {
        return new DownloadEntityDaoImpl(openHelper);
    }

    @Provides
    public IDao<CachedVideo> provideCachedVideo(SQLiteOpenHelper openHelper) {
        return new PersistentVideoDaoImpl(openHelper);
    }

    @Provides
    public IDao<BlockPersistentWrapper> provideBlockWrapper(SQLiteOpenHelper openHelper, IDao<CachedVideo> daoCached) {
        return new BlockDaoImpl(openHelper, daoCached);
    }

    @Provides
    public IDao<Step> provideStep(SQLiteOpenHelper openHelper,
                                  IDao<BlockPersistentWrapper> blockDao,
                                  IDao<Assignment> assignmentDao,
                                  IDao<Progress> progressDao) {
        return new StepDaoImpl(openHelper, blockDao, assignmentDao, progressDao);
    }
}
