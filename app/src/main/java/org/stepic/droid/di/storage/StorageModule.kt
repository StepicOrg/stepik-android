package org.stepic.droid.di.storage

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.di.qualifiers.ExternalVideoUrl
import org.stepic.droid.di.qualifiers.SavedVideoUrl
import org.stepic.droid.model.*
import org.stepic.droid.model.Unit
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.storage.DatabaseHelper
import org.stepic.droid.storage.dao.*
import org.stepic.droid.storage.structure.DbStructureVideoUrl
import org.stepic.droid.web.ViewAssignment

@Module
abstract class StorageModule {

    @StorageSingleton
    @Binds
    internal abstract fun provideSqlOpenHelper(databaseHelper: DatabaseHelper): SQLiteOpenHelper

    @StorageSingleton
    @Binds
    internal abstract fun provideSectionDao(sectionDao: SectionDaoImpl): IDao<Section>

    @StorageSingleton
    @Binds
    internal abstract fun provideUnitDao(unitDao: UnitDaoImpl): IDao<Unit>

    @StorageSingleton
    @Binds
    internal abstract fun provideProgressDao(progressDao: ProgressDaoImpl): IDao<Progress>

    @Binds
    internal abstract fun provideAssignmentDao(assignmentDao: AssignmentDaoImpl): IDao<Assignment>

    @Binds
    internal abstract fun provideCertificateDao(certificateViewItemDao: CertificateViewItemDaoImpl): IDao<CertificateViewItem>

    @Binds
    internal abstract fun provideLessonDao(lessonDao: LessonDaoImpl): IDao<Lesson>

    @StorageSingleton
    @Binds
    internal abstract fun provideViewAssignment(viewAssignmentDao: ViewAssignmentDaoImpl): IDao<ViewAssignment>

    @StorageSingleton
    @Binds
    internal abstract fun provideDownloadEntity(downloadEntityDao: DownloadEntityDaoImpl): IDao<DownloadEntity>

    @StorageSingleton
    @Binds
    internal abstract fun provideCalendarSection(calendarSectionDao: CalendarSectionDaoImpl): IDao<CalendarSection>

    @StorageSingleton
    @Binds
    internal abstract fun provideCachedVideo(persistentVideoDao: PersistentVideoDaoImpl): IDao<CachedVideo>

    @StorageSingleton
    @Binds
    internal abstract fun provideBlockWrapper(blockDao: BlockDaoImpl): IDao<BlockPersistentWrapper>

    @StorageSingleton
    @Binds
    internal abstract fun provideStep(stepDao: StepDaoImpl): IDao<Step>

    @Binds
    internal abstract fun provideCourse(courseDao: CourseDaoImpl): IDao<Course>

    @StorageSingleton
    @Binds
    internal abstract fun provideNotification(notificationDao: NotificationDaoImpl): IDao<Notification>

    @Binds
    @StorageSingleton
    internal abstract fun provideVideoTimeStamp(videoTimestampDao: VideoTimestampDaoImpl): IDao<VideoTimestamp>

    @Binds
    @StorageSingleton
    internal abstract fun provideLastStepDao(persistentLastStepDao: PersistentLastStepDaoImpl): IDao<PersistentLastStep>

    @Binds
    @StorageSingleton
    internal abstract fun provideCourseInteractionDao(courseLastInteractionDao: CourseLastInteractionDaoImpl): IDao<CourseLastInteraction>

    @Module
    companion object {
        @StorageSingleton
        @Provides
        @JvmStatic
        internal fun provideWritableDatabase(helper: SQLiteOpenHelper): SQLiteDatabase {
            return helper.writableDatabase
        }

        @StorageSingleton
        @Provides
        @JvmStatic
        @SavedVideoUrl
        internal fun provideSavedVideoUrlDao(writeableDatabase: SQLiteDatabase): IDao<DbVideoUrl> {
            return VideoUrlDao(writeableDatabase, DbStructureVideoUrl.savedVideosName)
        }

        @StorageSingleton
        @Provides
        @JvmStatic
        @ExternalVideoUrl
        internal fun provideExternalVideoUrlDao(writeableDatabase: SQLiteDatabase): IDao<DbVideoUrl> {
            return VideoUrlDao(writeableDatabase, DbStructureVideoUrl.externalVideosName)
        }
    }
}
