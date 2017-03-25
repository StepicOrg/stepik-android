package org.stepic.droid.core.modules;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.stepic.droid.model.Assignment;
import org.stepic.droid.model.BlockPersistentWrapper;
import org.stepic.droid.model.CachedVideo;
import org.stepic.droid.model.CalendarSection;
import org.stepic.droid.model.CertificateViewItem;
import org.stepic.droid.model.Course;
import org.stepic.droid.model.CourseLastInteraction;
import org.stepic.droid.model.DownloadEntity;
import org.stepic.droid.model.Lesson;
import org.stepic.droid.model.PersistentLastStep;
import org.stepic.droid.model.Progress;
import org.stepic.droid.model.Section;
import org.stepic.droid.model.Step;
import org.stepic.droid.model.Unit;
import org.stepic.droid.model.VideoTimestamp;
import org.stepic.droid.notifications.model.Notification;
import org.stepic.droid.store.DatabaseHelper;
import org.stepic.droid.store.dao.AssignmentDaoImpl;
import org.stepic.droid.store.dao.BlockDaoImpl;
import org.stepic.droid.store.dao.CalendarSectionDaoImpl;
import org.stepic.droid.store.dao.CertificateViewItemDaoImpl;
import org.stepic.droid.store.dao.CourseDaoImpl;
import org.stepic.droid.store.dao.CourseLastInteractionDaoImpl;
import org.stepic.droid.store.dao.DownloadEntityDaoImpl;
import org.stepic.droid.store.dao.IDao;
import org.stepic.droid.store.dao.LessonDaoImpl;
import org.stepic.droid.store.dao.NotificationDaoImpl;
import org.stepic.droid.store.dao.PersistentLastStepDaoImpl;
import org.stepic.droid.store.dao.PersistentVideoDaoImpl;
import org.stepic.droid.store.dao.ProgressDaoImpl;
import org.stepic.droid.store.dao.SectionDaoImpl;
import org.stepic.droid.store.dao.StepDaoImpl;
import org.stepic.droid.store.dao.UnitDaoImpl;
import org.stepic.droid.store.dao.VideoTimestampDaoImpl;
import org.stepic.droid.store.dao.ViewAssignmentDaoImpl;
import org.stepic.droid.store.operations.DatabaseFacade;
import org.stepic.droid.web.ViewAssignment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class StorageModule {

    @Singleton
    @Provides
    DatabaseFacade provideDatabaseFacade() {
        return new DatabaseFacade();
    }

    @Singleton
    @Provides
    SQLiteOpenHelper provideSqlOpenHelper(Context context) {
        return new DatabaseHelper(context);
    }

    @Singleton
    @Provides
    SQLiteDatabase provideWritableDatabase(SQLiteOpenHelper helper) {
        return helper.getWritableDatabase();
    }

    @Singleton
    @Provides
    IDao<Section> provideSectionDao(SQLiteDatabase openHelper) {
        return new SectionDaoImpl(openHelper);
    }

    @Singleton
    @Provides
    IDao<Unit> provideUnitDao(SQLiteDatabase openHelper, IDao<Progress> progressDao) {
        return new UnitDaoImpl(openHelper, progressDao);
    }

    @Singleton
    @Provides
    IDao<Progress> provideProgressDao(SQLiteDatabase openHelper) {
        return new ProgressDaoImpl(openHelper);
    }

    @Provides
    IDao<Assignment> provideAssignmentDao(SQLiteDatabase openHelper) {
        return new AssignmentDaoImpl(openHelper);
    }

    @Provides
    IDao<CertificateViewItem> provideCertificateDao(SQLiteDatabase openHelper) {
        return new CertificateViewItemDaoImpl(openHelper);
    }

    @Provides
    IDao<Lesson> provideLessonDao(SQLiteDatabase openHelper) {
        return new LessonDaoImpl(openHelper);
    }

    @Singleton
    @Provides
    IDao<ViewAssignment> provideViewAssignment(SQLiteDatabase openHelper) {
        return new ViewAssignmentDaoImpl(openHelper);
    }

    @Singleton
    @Provides
    IDao<DownloadEntity> provideDownloadEntity(SQLiteDatabase openHelper) {
        return new DownloadEntityDaoImpl(openHelper);
    }

    @Singleton
    @Provides
    IDao<CalendarSection> provideCalendarSection(SQLiteDatabase database) {
        return new CalendarSectionDaoImpl(database);
    }

    @Singleton
    @Provides
    IDao<CachedVideo> provideCachedVideo(SQLiteDatabase openHelper) {
        return new PersistentVideoDaoImpl(openHelper);
    }

    @Singleton
    @Provides
    IDao<BlockPersistentWrapper> provideBlockWrapper(SQLiteDatabase openHelper, IDao<CachedVideo> daoCached) {
        return new BlockDaoImpl(openHelper, daoCached);
    }

    @Singleton
    @Provides
    IDao<Step> provideStep(SQLiteDatabase openHelper,
                           IDao<BlockPersistentWrapper> blockDao,
                           IDao<Assignment> assignmentDao,
                           IDao<Progress> progressDao) {
        return new StepDaoImpl(openHelper, blockDao, assignmentDao, progressDao);
    }

    @Provides
    IDao<Course> provideCourse(SQLiteDatabase openHelper, IDao<CachedVideo> daoCached) {
        return new CourseDaoImpl(openHelper, daoCached);
    }

    @Singleton
    @Provides
    IDao<Notification> provideNotification(SQLiteDatabase openHelper) {
        return new NotificationDaoImpl(openHelper);
    }

    @Provides
    @Singleton
    IDao<VideoTimestamp> provideVideoTimeStamp(SQLiteDatabase SQLiteDatabase) {
        return new VideoTimestampDaoImpl(SQLiteDatabase);
    }

    @Provides
    @Singleton
    IDao<PersistentLastStep> provideLastStepDao(SQLiteDatabase sqLiteDatabase) {
        return new PersistentLastStepDaoImpl(sqLiteDatabase);
    }

    @Provides
    @Singleton
    IDao<CourseLastInteraction> provideCourseInteractionDao(SQLiteDatabase sqLiteDatabase) {
        return new CourseLastInteractionDaoImpl(sqLiteDatabase);
    }
}
