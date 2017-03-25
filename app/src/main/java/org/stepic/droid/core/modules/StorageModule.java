package org.stepic.droid.core.modules;

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
import org.stepic.droid.web.ViewAssignment;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class StorageModule {

    @Singleton
    @Binds
    abstract SQLiteOpenHelper provideSqlOpenHelper(DatabaseHelper databaseHelper);

    @Singleton
    @Provides
    static SQLiteDatabase provideWritableDatabase(SQLiteOpenHelper helper) {
        return helper.getWritableDatabase();
    }

    @Singleton
    @Binds
    abstract IDao<Section> provideSectionDao(SectionDaoImpl sectionDao);

    @Singleton
    @Binds
    abstract IDao<Unit> provideUnitDao(UnitDaoImpl unitDao);

    @Singleton
    @Binds
    abstract IDao<Progress> provideProgressDao(ProgressDaoImpl progressDao);

    @Binds
    abstract IDao<Assignment> provideAssignmentDao(AssignmentDaoImpl assignmentDao);

    @Binds
    abstract IDao<CertificateViewItem> provideCertificateDao(CertificateViewItemDaoImpl certificateViewItemDao);

    @Binds
    abstract IDao<Lesson> provideLessonDao(LessonDaoImpl lessonDao);

    @Singleton
    @Binds
    abstract IDao<ViewAssignment> provideViewAssignment(ViewAssignmentDaoImpl viewAssignmentDao);

    @Singleton
    @Binds
    abstract IDao<DownloadEntity> provideDownloadEntity(DownloadEntityDaoImpl downloadEntityDao);

    @Singleton
    @Binds
    abstract IDao<CalendarSection> provideCalendarSection(CalendarSectionDaoImpl calendarSectionDao);

    @Singleton
    @Binds
    abstract IDao<CachedVideo> provideCachedVideo(PersistentVideoDaoImpl persistentVideoDao);

    @Singleton
    @Binds
    abstract IDao<BlockPersistentWrapper> provideBlockWrapper(BlockDaoImpl blockDao);

    @Singleton
    @Binds
    abstract IDao<Step> provideStep(StepDaoImpl stepDao);

    @Binds
    abstract IDao<Course> provideCourse(CourseDaoImpl courseDao);

    @Singleton
    @Binds
    abstract IDao<Notification> provideNotification(NotificationDaoImpl notificationDao);

    @Binds
    @Singleton
    abstract IDao<VideoTimestamp> provideVideoTimeStamp(VideoTimestampDaoImpl videoTimestampDao);

    @Binds
    @Singleton
    abstract IDao<PersistentLastStep> provideLastStepDao(PersistentLastStepDaoImpl persistentLastStepDao);

    @Binds
    @Singleton
    abstract IDao<CourseLastInteraction> provideCourseInteractionDao(CourseLastInteractionDaoImpl courseLastInteractionDao);
}
