package org.stepic.droid.core;

import android.app.DownloadManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.squareup.otto.Bus;

import org.stepic.droid.analytic.Analytic;
import org.stepic.droid.analytic.AnalyticImpl;
import org.stepic.droid.concurrency.IMainHandler;
import org.stepic.droid.concurrency.MainHandlerImpl;
import org.stepic.droid.configuration.ConfigRelease;
import org.stepic.droid.configuration.IConfig;
import org.stepic.droid.model.Assignment;
import org.stepic.droid.model.BlockPersistentWrapper;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.notifications.INotificationManager;
import org.stepic.droid.notifications.NotificationManagerImpl;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.preferences.SharedPreferenceHelper;
import org.stepic.droid.preferences.UserPreferences;
import org.stepic.droid.presenters.course_finder.CourseFinderPresenter;
import org.stepic.droid.presenters.course_finder.CourseFinderPresenterForDetailScreen;
import org.stepic.droid.presenters.course_finder.CourseFinderPresenterForSectionScreen;
import org.stepic.droid.presenters.course_joiner.CourseJoinerPresenter;
import org.stepic.droid.presenters.course_joiner.CourseJoinerPresenterImpl;
import org.stepic.droid.social.SocialManager;
import org.stepic.droid.store.CleanManager;
import org.stepic.droid.store.ConcurrentCancelSniffer;
import org.stepic.droid.store.DatabaseHelper;
import org.stepic.droid.store.DownloadManagerImpl;
import org.stepic.droid.store.ICancelSniffer;
import org.stepic.droid.store.IDownloadManager;
import org.stepic.droid.store.IStoreStateManager;
import org.stepic.droid.store.StoreStateManager;
import org.stepic.droid.store.dao.AssignmentDaoImpl;
import org.stepic.droid.store.dao.BlockDaoImpl;
import org.stepic.droid.store.dao.CourseDaoImpl;
import org.stepic.droid.store.dao.DownloadEntityDaoImpl;
import org.stepic.droid.store.dao.IDao;
import org.stepic.droid.store.dao.LessonDaoImpl;
import org.stepic.droid.store.dao.NotificationDaoImpl;
import org.stepic.droid.store.dao.PersistentVideoDaoImpl;
import org.stepic.droid.store.dao.ProgressDaoImpl;
import org.stepic.droid.store.dao.SectionDaoImpl;
import org.stepic.droid.store.dao.StepDaoImpl;
import org.stepic.droid.store.dao.UnitDaoImpl;
import org.stepic.droid.store.dao.ViewAssignmentDaoImpl;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.util.AppConstants;
import org.stepic.droid.util.resolvers.CoursePropertyResolver;
import org.stepic.droid.util.resolvers.ISearchResolver;
import org.stepic.droid.util.resolvers.IStepResolver;
import org.stepic.droid.util.resolvers.IVideoResolver;
import org.stepic.droid.util.resolvers.SearchResolver;
import org.stepic.droid.util.resolvers.StepTypeResolver;
import org.stepic.droid.util.resolvers.VideoResolver;
import org.stepic.droid.web.IApi;
import org.stepic.droid.web.RetrofitRESTApi;
import org.stepic.droid.web.ViewAssignment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Named;
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
    public IScreenManager provideIScreenManager(IConfig config, UserPreferences userPreferences, Analytic analytic) {
        return new ScreenManager(config, userPreferences, analytic);
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
    public IVideoResolver provideVideoResolver(Analytic analytic,
                                               DatabaseFacade dbOperationsCachedVideo,
                                               UserPreferences userPreferences,
                                               CleanManager cleanManager) {
        return new VideoResolver(dbOperationsCachedVideo, userPreferences, cleanManager, analytic);
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
    public DatabaseFacade provideDbOperationCachedVideo() {
        return new DatabaseFacade();
    }

    @Singleton
    @Provides
    public IStoreStateManager provideStoreManager(DatabaseFacade dbManager, Bus bus) {
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
    public ILocalProgressManager provideProgressManager(DatabaseFacade databaseFacade, Bus bus, IApi api) {
        return new LocalProgressOfUnitManager(databaseFacade, bus, api);
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

    @Singleton
    @Provides
    public SQLiteDatabase provideWritableDatabase(DatabaseHelper helper) {
        return helper.getWritableDatabase();
    }

    @Provides
    public IDao<Section> provideSectionDao(SQLiteDatabase openHelper) {
        return new SectionDaoImpl(openHelper);
    }

    @Provides
    public IDao<Unit> provideUnitDao(SQLiteDatabase openHelper, IDao<Progress> progressDao) {
        return new UnitDaoImpl(openHelper, progressDao);
    }

    @Provides
    public IDao<Progress> provideProgressDao(SQLiteDatabase openHelper) {
        return new ProgressDaoImpl(openHelper);
    }

    @Provides
    public IDao<Assignment> provideAssignmentDao(SQLiteDatabase openHelper) {
        return new AssignmentDaoImpl(openHelper);
    }

    @Provides
    public IDao<Lesson> provideLessonDao(SQLiteDatabase openHelper) {
        return new LessonDaoImpl(openHelper);
    }

    @Provides
    public IDao<ViewAssignment> provideViewAssignment(SQLiteDatabase openHelper) {
        return new ViewAssignmentDaoImpl(openHelper);
    }

    @Provides
    public IDao<DownloadEntity> provideDownloadEntity(SQLiteDatabase openHelper) {
        return new DownloadEntityDaoImpl(openHelper);
    }

    @Provides
    public IDao<CachedVideo> provideCachedVideo(SQLiteDatabase openHelper) {
        return new PersistentVideoDaoImpl(openHelper);
    }

    @Provides
    public IDao<BlockPersistentWrapper> provideBlockWrapper(SQLiteDatabase openHelper, IDao<CachedVideo> daoCached) {
        return new BlockDaoImpl(openHelper, daoCached);
    }

    @Provides
    public IDao<Step> provideStep(SQLiteDatabase openHelper,
                                  IDao<BlockPersistentWrapper> blockDao,
                                  IDao<Assignment> assignmentDao,
                                  IDao<Progress> progressDao) {
        return new StepDaoImpl(openHelper, blockDao, assignmentDao, progressDao);
    }

    @Provides
    public IDao<Course> provideCourse(SQLiteDatabase openHelper, IDao<CachedVideo> daoCached) {
        return new CourseDaoImpl(openHelper, daoCached);
    }

    @Provides
    public IDao<Notification> provideNotification(SQLiteDatabase openHelper) {
        return new NotificationDaoImpl(openHelper);
    }

    @Provides
    @Singleton
    public ICancelSniffer provideCancelSniffer() {
        return new ConcurrentCancelSniffer();
    }

    @Provides
    @Singleton
    public ExecutorService provideSingle() {
        return Executors.newSingleThreadExecutor();
    }


    //it is good for many short lived, which should do async
    @Provides
    @Singleton
    public ThreadPoolExecutor provideThreadPool() {
        return (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    @Singleton
    @Provides
    public IMainHandler provideHandlerForUIThread() {
        return new MainHandlerImpl();
    }

    @Singleton
    @Provides
    public AudioFocusHelper provideAudioFocusHelper(Context context, IMainHandler mainHandler, Bus bus) {
        return new AudioFocusHelper(context, bus, mainHandler);
    }

    @Singleton
    @Provides
    public INotificationManager provideNotificationManager(SharedPreferenceHelper sp, IApi api, IConfig config, UserPreferences userPreferences, DatabaseFacade db) {
        return new NotificationManagerImpl(sp, api, config, userPreferences, db);
    }

    @Provides
    public CommentManager provideCommentsManager() {
        return new CommentManager();
    }

    @Singleton
    @Provides
    @Named(AppConstants.SECTION_NAMED_INJECTION_COURSE_FINDER)
    public CourseFinderPresenter provideCourseFinderPresenterForSections() {
        return new CourseFinderPresenterForSectionScreen();
    }

    @Singleton
    @Provides
    @Named(AppConstants.ABOUT_NAME_INJECTION_COURSE_FINDER)
    public CourseFinderPresenter provideCourseFinderPresenterForDetailScreen() {
        return new CourseFinderPresenterForDetailScreen();
    }

    @Provides
    public CourseJoinerPresenter provideCourseJoiner() {
        return new CourseJoinerPresenterImpl();
    }

    @Provides
    @Singleton
    public Analytic provideAnalytic(Context context) {
        return new AnalyticImpl(context);
    }
}
