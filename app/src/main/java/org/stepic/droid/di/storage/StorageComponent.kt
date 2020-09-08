package org.stepic.droid.di.storage

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.stepic.droid.features.stories.model.ViewedStoryTemplate
import org.stepic.droid.persistence.storage.dao.PersistentItemDao
import org.stepic.droid.persistence.storage.dao.PersistentStateDao
import org.stepic.droid.storage.dao.IDao
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.cache.download.dao.DownloadedCoursesDao
import org.stepik.android.cache.personal_deadlines.dao.DeadlinesBannerDao
import org.stepik.android.cache.personal_deadlines.dao.PersonalDeadlinesDao
import org.stepik.android.data.course_list.model.CourseListQueryData
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.model.Certificate
import org.stepik.android.model.CourseReviewSummary
import org.stepik.android.model.SocialProfile
import org.stepik.android.model.Submission
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.model.CourseCollection
import org.stepik.android.cache.base.database.AnalyticDatabase
import org.stepik.android.cache.base.database.AppDatabase
import org.stepik.android.cache.purchase_notification.dao.PurchaseNotificationDao
import org.stepik.android.domain.course_payments.model.CoursePayment
import org.stepik.android.model.attempts.Attempt
import org.stepik.android.model.comments.DiscussionThread
import org.stepik.android.model.user.User

@Component(modules = [StorageModule::class])
@StorageSingleton
interface StorageComponent {

    @Component.Builder
    interface Builder {
        fun build(): StorageComponent

        @BindsInstance
        fun context(context: Context): Builder
    }

    val databaseFacade: DatabaseFacade

    val deadlinesDao: PersonalDeadlinesDao
    val deadlinesBannerDao: DeadlinesBannerDao
    val persistentItemDao: PersistentItemDao
    val persistentStateDao: PersistentStateDao

    val courseReviewsDao: IDao<CourseReview>
    val userDao: IDao<User>

    val viewedStoryTemplatesDao: IDao<ViewedStoryTemplate>
    val courseReviewSummaryDao: IDao<CourseReviewSummary>
    val submissionDao: IDao<Submission>
    val certificateDao: IDao<Certificate>
    val discussionThreadDao: IDao<DiscussionThread>
    val attemptDao: IDao<Attempt>
    val downloadedCoursesDao: DownloadedCoursesDao
    val socialProfileDao: IDao<SocialProfile>
    val userCourseDao: IDao<UserCourse>
    val courseCollectionDao: IDao<CourseCollection>
    val courseListQueryDao: IDao<CourseListQueryData>
    val purchaseNotificationDao: PurchaseNotificationDao
    val coursePaymentDao: IDao<CoursePayment>

    val analyticDatabase: AnalyticDatabase
    val appDatabase: AppDatabase
}
