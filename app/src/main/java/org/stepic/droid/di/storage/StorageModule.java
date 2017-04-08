package org.stepic.droid.di.storage;

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
import org.stepic.droid.storage.DatabaseHelper;
import org.stepic.droid.storage.dao.AssignmentDaoImpl;
import org.stepic.droid.storage.dao.BlockDaoImpl;
import org.stepic.droid.storage.dao.CalendarSectionDaoImpl;
import org.stepic.droid.storage.dao.CertificateViewItemDaoImpl;
import org.stepic.droid.storage.dao.CourseDaoImpl;
import org.stepic.droid.storage.dao.CourseLastInteractionDaoImpl;
import org.stepic.droid.storage.dao.DownloadEntityDaoImpl;
import org.stepic.droid.storage.dao.IDao;
import org.stepic.droid.storage.dao.LessonDaoImpl;
import org.stepic.droid.storage.dao.NotificationDaoImpl;
import org.stepic.droid.storage.dao.PersistentLastStepDaoImpl;
import org.stepic.droid.storage.dao.PersistentVideoDaoImpl;
import org.stepic.droid.storage.dao.ProgressDaoImpl;
import org.stepic.droid.storage.dao.SectionDaoImpl;
import org.stepic.droid.storage.dao.StepDaoImpl;
import org.stepic.droid.storage.dao.UnitDaoImpl;
import org.stepic.droid.storage.dao.VideoTimestampDaoImpl;
import org.stepic.droid.storage.dao.ViewAssignmentDaoImpl;
import org.stepic.droid.web.ViewAssignment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class StorageModule {

    @StorageSingleton
    @Binds
    abstract SQLiteOpenHelper provideSqlOpenHelper(DatabaseHelper databaseHelper);

    @StorageSingleton
    @Provides
    static SQLiteDatabase provideWritableDatabase(SQLiteOpenHelper helper) {
        return helper.getWritableDatabase();
    }

    @StorageSingleton
    @Binds
    abstract IDao<Section> provideSectionDao(SectionDaoImpl sectionDao);

    @StorageSingleton
    @Binds
    abstract IDao<Unit> provideUnitDao(UnitDaoImpl unitDao);

    @StorageSingleton
    @Binds
    abstract IDao<Progress> provideProgressDao(ProgressDaoImpl progressDao);

    @Binds
    abstract IDao<Assignment> provideAssignmentDao(AssignmentDaoImpl assignmentDao);

    @Binds
    abstract IDao<CertificateViewItem> provideCertificateDao(CertificateViewItemDaoImpl certificateViewItemDao);

    @Binds
    abstract IDao<Lesson> provideLessonDao(LessonDaoImpl lessonDao);

    @StorageSingleton
    @Binds
    abstract IDao<ViewAssignment> provideViewAssignment(ViewAssignmentDaoImpl viewAssignmentDao);

    @StorageSingleton
    @Binds
    abstract IDao<DownloadEntity> provideDownloadEntity(DownloadEntityDaoImpl downloadEntityDao);

    @StorageSingleton
    @Binds
    abstract IDao<CalendarSection> provideCalendarSection(CalendarSectionDaoImpl calendarSectionDao);

    @StorageSingleton
    @Binds
    abstract IDao<CachedVideo> provideCachedVideo(PersistentVideoDaoImpl persistentVideoDao);

    @StorageSingleton
    @Binds
    abstract IDao<BlockPersistentWrapper> provideBlockWrapper(BlockDaoImpl blockDao);

    @StorageSingleton
    @Binds
    abstract IDao<Step> provideStep(StepDaoImpl stepDao);

    @Binds
    abstract IDao<Course> provideCourse(CourseDaoImpl courseDao);

    @StorageSingleton
    @Binds
    abstract IDao<Notification> provideNotification(NotificationDaoImpl notificationDao);

    @Binds
    @StorageSingleton
    abstract IDao<VideoTimestamp> provideVideoTimeStamp(VideoTimestampDaoImpl videoTimestampDao);

    @Binds
    @StorageSingleton
    abstract IDao<PersistentLastStep> provideLastStepDao(PersistentLastStepDaoImpl persistentLastStepDao);

    @Binds
    @StorageSingleton
    abstract IDao<CourseLastInteraction> provideCourseInteractionDao(CourseLastInteractionDaoImpl courseLastInteractionDao);
}
