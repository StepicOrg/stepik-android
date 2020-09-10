package org.stepic.droid.di.storage

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import org.stepic.droid.features.stories.model.ViewedStoryTemplate
import org.stepic.droid.features.stories.storage.dao.ViewedStoryTemplatesDaoImpl
import org.stepic.droid.jsonHelpers.adapters.UTCDateAdapter
import org.stepic.droid.model.BlockPersistentWrapper
import org.stepic.droid.model.ViewedNotification
import org.stepic.droid.notifications.model.Notification
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.PersistentItemDaoImpl
import org.stepic.droid.persistence.storage.dao.PersistentStateDao
import org.stepic.droid.persistence.storage.dao.PersistentStateDaoImpl
import org.stepic.droid.storage.dao.AdaptiveExpDao
import org.stepic.droid.storage.dao.AdaptiveExpDaoImpl
import org.stepic.droid.storage.dao.AssignmentDaoImpl
import org.stepic.droid.storage.dao.BlockDaoImpl
import org.stepic.droid.storage.dao.CourseDaoImpl
import org.stepic.droid.storage.dao.CourseReviewSummaryDaoImpl
import org.stepic.droid.storage.dao.CourseReviewsDaoImpl
import org.stepic.droid.storage.dao.IDao
import org.stepic.droid.storage.dao.LastStepDaoImpl
import org.stepic.droid.storage.dao.LessonDaoImpl
import org.stepic.droid.storage.dao.NotificationDaoImpl
import org.stepic.droid.storage.dao.ProgressDaoImpl
import org.stepic.droid.storage.dao.SearchQueryDao
import org.stepic.droid.storage.dao.SearchQueryDaoImpl
import org.stepic.droid.storage.dao.SectionDaoImpl
import org.stepic.droid.storage.dao.SectionDateEventDaoImpl
import org.stepic.droid.storage.dao.StepDaoImpl
import org.stepic.droid.storage.dao.UnitDaoImpl
import org.stepic.droid.storage.dao.VideoTimestampDaoImpl
import org.stepic.droid.storage.dao.ViewAssignmentDaoImpl
import org.stepic.droid.storage.dao.ViewedNotificationsQueueDaoImpl
import org.stepic.droid.storage.migration.Migrations
import org.stepic.droid.storage.operations.DatabaseOperations
import org.stepic.droid.storage.operations.DatabaseOperationsImpl
import org.stepik.android.cache.attempt.dao.AttemptDaoImpl
import org.stepik.android.cache.certificates.dao.CertificateDaoImpl
import org.stepik.android.cache.course_collection.dao.CourseCollectionDaoImpl
import org.stepik.android.cache.course_list.dao.CourseListQueryDaoImpl
import org.stepik.android.cache.discussion_thread.dao.DiscussionThreadDaoImpl
import org.stepik.android.cache.download.dao.DownloadedCoursesDao
import org.stepik.android.cache.download.dao.DownloadedCoursesDaoImpl
import org.stepik.android.cache.personal_deadlines.dao.DeadlinesBannerDao
import org.stepik.android.cache.personal_deadlines.dao.DeadlinesBannerDaoImpl
import org.stepik.android.cache.personal_deadlines.dao.PersonalDeadlinesDao
import org.stepik.android.cache.personal_deadlines.dao.PersonalDeadlinesDaoImpl
import org.stepik.android.cache.social_profile.dao.SocialProfileDaoImpl
import org.stepik.android.cache.submission.dao.SubmissionDaoImpl
import org.stepik.android.cache.user.dao.UserDaoImpl
import org.stepik.android.cache.user_courses.dao.UserCourseDaoImpl
import org.stepik.android.cache.video.dao.VideoDao
import org.stepik.android.cache.video.dao.VideoDaoImpl
import org.stepik.android.cache.video.dao.VideoEntityDaoImpl
import org.stepik.android.cache.video.dao.VideoUrlEntityDaoImpl
import org.stepik.android.cache.video.model.VideoEntity
import org.stepik.android.cache.video.model.VideoUrlEntity
import org.stepik.android.cache.video_player.model.VideoTimestamp
import org.stepik.android.data.course_list.model.CourseListQueryData
import org.stepik.android.domain.course_calendar.model.SectionDateEvent
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.last_step.model.LastStep
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.*
import org.stepik.android.model.Unit
import org.stepik.android.cache.base.database.AnalyticDatabase
import org.stepik.android.cache.base.database.AnalyticDatabaseInfo
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.course_payments.dao.CoursePaymentsDaoImpl
import org.stepik.android.cache.purchase_notification.dao.PurchaseNotificationDao
import org.stepik.android.cache.purchase_notification.dao.PurchaseNotificationDaoImpl
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.model.user.User

@Module
abstract class StorageModule {

    @StorageSingleton
    @Binds
    internal abstract fun bindsOperations(databaseOperationsImpl: DatabaseOperationsImpl): DatabaseOperations

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
    internal abstract fun provideLessonDao(lessonDao: LessonDaoImpl): IDao<Lesson>

    @StorageSingleton
    @Binds
    internal abstract fun provideViewAssignment(viewAssignmentDao: ViewAssignmentDaoImpl): IDao<ViewAssignment>

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

    @StorageSingleton
    @Binds
    internal abstract fun bindUserDao(userDaoImpl: UserDaoImpl): IDao<User>

    @StorageSingleton
    @Binds
    internal abstract fun bindSectionDateEventDao(sectionDateEventDaoImpl: SectionDateEventDaoImpl): IDao<SectionDateEvent>

    @StorageSingleton
    @Binds
    internal abstract fun bindCourseReviewsDao(courseReviewsDaoImpl: CourseReviewsDaoImpl): IDao<CourseReview>

    @StorageSingleton
    @Binds
    internal abstract fun bindCourseReviewSummaryDao(courseReviewSummaryDaoImpl: CourseReviewSummaryDaoImpl): IDao<CourseReviewSummary>

    @StorageSingleton
    @Binds
    internal abstract fun bindSubmissionDao(submissionDaoImpl: SubmissionDaoImpl): IDao<Submission>

    @StorageSingleton
    @Binds
    internal abstract fun bindCertificateDao(certificateDaoImpl: CertificateDaoImpl): IDao<Certificate>

    @StorageSingleton
    @Binds
    internal abstract fun bindDiscussionThreadDao(discussionThreadDaoImpl: DiscussionThreadDaoImpl): IDao<DiscussionThread>

    @StorageSingleton
    @Binds
    internal abstract fun bindAttemptDao(attemptDaoImpl: AttemptDaoImpl): IDao<Attempt>

    @StorageSingleton
    @Binds
    internal abstract fun bindDownloadeCoursesDao(downloadedCoursesDaoImpl: DownloadedCoursesDaoImpl): DownloadedCoursesDao

    @StorageSingleton
    @Binds
    internal abstract fun bindSocialProfileDao(socialProfileDaoImpl: SocialProfileDaoImpl): IDao<SocialProfile>

    @StorageSingleton
    @Binds
    internal abstract fun bindUserCourseDao(userCourseDao: UserCourseDaoImpl): IDao<UserCourse>

    @StorageSingleton
    @Binds
    internal abstract fun bindCourseCollectionDao(courseCollectionDaoImpl: CourseCollectionDaoImpl): IDao<CourseCollection>

    @StorageSingleton
    @Binds
    internal abstract fun bindCourseListQueryDataDao(courseListQueryDataDaoImpl: CourseListQueryDaoImpl): IDao<CourseListQueryData>

    @StorageSingleton
    @Binds
    internal abstract fun bindPurchaseNotificationDao(purchaseNotificationDaoImpl: PurchaseNotificationDaoImpl): PurchaseNotificationDao

    @StorageSingleton
    @Binds
    internal abstract fun bindCoursePaymentDao(coursePaymentsDaoImpl: CoursePaymentsDaoImpl): IDao<CoursePayment>

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
        internal fun provideAnalyticDatabase(context: Context): AnalyticDatabase =
            Room.databaseBuilder(context, AnalyticDatabase::class.java, AnalyticDatabaseInfo.DATABASE_NAME).build()

        @StorageSingleton
        @Provides
        @JvmStatic
        internal fun provideAppDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
                .addMigrations(*Migrations.migrations)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        Migrations.migrations.forEach { it.migrate(db) }
                    }
                })
                .build()

        @StorageSingleton
        @Provides
        @JvmStatic
        internal fun provideSqlOpenHelper(appDatabase: AppDatabase): SupportSQLiteOpenHelper =
            appDatabase.openHelper

        @StorageSingleton
        @Provides
        @JvmStatic
        internal fun provideWritableDatabase(helper: SupportSQLiteOpenHelper): SupportSQLiteDatabase =
            helper.writableDatabase
    }
}
