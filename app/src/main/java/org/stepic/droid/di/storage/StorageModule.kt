package org.stepic.droid.di.storage

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.features.deadlines.storage.dao.DeadlinesBannerDao
import org.stepic.droid.features.deadlines.storage.dao.DeadlinesBannerDaoImpl
import org.stepic.droid.features.deadlines.storage.operations.DeadlinesRecordOperations
import org.stepic.droid.features.deadlines.storage.operations.DeadlinesRecordOperationsImpl
import org.stepic.droid.features.deadlines.storage.dao.PersonalDeadlinesDao
import org.stepic.droid.features.deadlines.storage.dao.PersonalDeadlinesDaoImpl
import org.stepic.droid.features.stories.model.ViewedStoryTemplate
import org.stepic.droid.features.stories.storage.dao.ViewedStoryTemplatesDaoImpl
import org.stepic.droid.jsonHelpers.adapters.UTCDateAdapter
import org.stepic.droid.model.*
import org.stepic.droid.model.code.CodeSubmission
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.PersistentItemDaoImpl
import org.stepic.droid.persistence.storage.dao.PersistentStateDao
import org.stepic.droid.persistence.storage.dao.PersistentStateDaoImpl
import org.stepic.droid.storage.DatabaseHelper
import org.stepic.droid.storage.dao.*
import org.stepic.droid.storage.operations.*
import org.stepic.droid.web.ViewAssignment
import org.stepik.android.cache.video.dao.VideoEntityDaoImpl
import org.stepik.android.cache.video.dao.VideoDao
import org.stepik.android.cache.video.dao.VideoDaoImpl
import org.stepik.android.cache.video.dao.VideoUrlEntityDaoImpl
import org.stepik.android.cache.video.model.VideoEntity
import org.stepik.android.cache.video.model.VideoUrlEntity
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.model.*
import org.stepik.android.model.Unit

@Module
abstract class StorageModule {

    @StorageSingleton
    @Binds
    internal abstract fun bindsOperations(databaseOperationsImpl: DatabaseOperationsImpl): DatabaseOperations

    @StorageSingleton
    @Binds
    internal abstract fun provideSqlOpenHelper(databaseHelper: DatabaseHelper): SQLiteOpenHelper


    @StorageSingleton
    @Binds
    internal abstract fun provideCodeSubmissionDao(codeSubmissionDaoImpl: CodeSubmissionDaoImpl): IDao<CodeSubmission>

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
    internal abstract fun provideCalendarSection(calendarSectionDao: CalendarSectionDaoImpl): IDao<CalendarSection>

    @StorageSingleton
    @Binds
    internal abstract fun provideBlockWrapper(blockDao: BlockDaoImpl): IDao<BlockPersistentWrapper>

    @StorageSingleton
    @Binds
    internal abstract fun provideStep(stepDao: StepDaoImpl): IDao<Step>

    @StorageSingleton
    @Binds
    internal abstract fun provideNotification(notificationDao: NotificationDaoImpl): IDao<Notification>

    @Binds
    @StorageSingleton
    internal abstract fun provideVideoTimeStamp(videoTimestampDao: VideoTimestampDaoImpl): IDao<VideoTimestamp>

    @Binds
    @StorageSingleton
    internal abstract fun provideLastStepDao(lastStepDao: LastStepDaoImpl): IDao<LastStep>

    @Binds
    @StorageSingleton
    internal abstract fun provideExternalVideoUrlDao(videoUrlDao: VideoUrlDaoImpl): IDao<DbVideoUrl>

    @StorageSingleton
    @Binds
    internal abstract fun provideSearchQueryDao(searchQueryDaoImpl: SearchQueryDaoImpl): SearchQueryDao

    @StorageSingleton
    @Binds
    internal abstract fun provideAdaptiveExpDao(adaptiveExpDaoImpl: AdaptiveExpDaoImpl): AdaptiveExpDao

    @StorageSingleton
    @Binds
    internal abstract fun provideViewedNotificationsQueueDao(viewedNotificationsQueueDaoImpl: ViewedNotificationsQueueDaoImpl): IDao<ViewedNotification>

    @StorageSingleton
    @Binds
    internal abstract fun providePersonalDeadlinesDao(personalDeadlinesDaoImpl: PersonalDeadlinesDaoImpl): PersonalDeadlinesDao

    @StorageSingleton
    @Binds
    internal abstract fun provideDeadlinesRecordOperations(deadlinesRecordOperationsImpl: DeadlinesRecordOperationsImpl): DeadlinesRecordOperations

    @StorageSingleton
    @Binds
    internal abstract fun provideDeadlinesBannerDao(deadlinesBannerDaoImpl: DeadlinesBannerDaoImpl): DeadlinesBannerDao

    @StorageSingleton
    @Binds
    internal abstract fun providePersistentItemDao(persistentItemDaoImpl: PersistentItemDaoImpl): PersistentItemDao

    @StorageSingleton
    @Binds
    internal abstract fun providePersistentStateDao(persistentStateDaoImpl: PersistentStateDaoImpl): PersistentStateDao

    @StorageSingleton
    @Binds
    internal abstract fun provideViewedStoryTemplatesDao(viewedStoryTemplatesDaoImpl: ViewedStoryTemplatesDaoImpl): IDao<ViewedStoryTemplate>

    @StorageSingleton
    @Binds
    internal abstract fun bindCourseListDao(courseListDaoImpl: CourseListDaoImpl): CourseListDao

    @StorageSingleton
    @Binds
    internal abstract fun bindCourseDao(courseDaoImpl: CourseDaoImpl): IDao<Course>

    @StorageSingleton
    @Binds
    internal abstract fun bindVideoDao(videoDaoImpl: VideoDaoImpl): VideoDao

    @StorageSingleton
    @Binds
    internal abstract fun bindVideoEntityDao(videoEntityDaoImpl: VideoEntityDaoImpl): IDao<VideoEntity>

    @StorageSingleton
    @Binds
    internal abstract fun bindVideoUrlEntityDao(videoUrlEntityDaoImpl: VideoUrlEntityDaoImpl): IDao<VideoUrlEntity>

    @Module
    companion object {

        @StorageSingleton
        @Provides
        @JvmStatic
        internal fun provideStorageGson(): Gson =
                GsonBuilder()
                        .enableComplexMapKeySerialization()
                        .create()

        @StorageSingleton
        @Provides
        @JvmStatic
        internal fun provideDateAdapter() = UTCDateAdapter()

        @StorageSingleton
        @Provides
        @JvmStatic
        internal fun provideWritableDatabase(helper: SQLiteOpenHelper): SQLiteDatabase =
                helper.writableDatabase
    }
}
