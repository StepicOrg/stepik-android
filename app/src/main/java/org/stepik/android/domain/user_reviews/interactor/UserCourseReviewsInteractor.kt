package org.stepik.android.domain.user_reviews.interactor

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.preferences.UserPreferences
import org.stepic.droid.util.safeDiv
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_list.interactor.CourseListUserInteractor
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.course_reviews.repository.CourseReviewsRepository
import org.stepik.android.domain.profile.model.ProfileData
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.progress.mapper.getProgresses
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import org.stepik.android.domain.user_reviews.model.UserCourseReviewsResult
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.view.injection.user_reviews.LearningActionsScope
import javax.inject.Inject

@LearningActionsScope
class UserCourseReviewsInteractor
@Inject
constructor(
    private val userPreferences: UserPreferences,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    private val courseReviewsRepository: CourseReviewsRepository,
    private val courseListUserInteractor: CourseListUserInteractor,
    private val profileRepository: ProfileRepository,
    private val progressRepository: ProgressRepository
) {

    fun fetchUserCourseReviewItems(primaryDataSourceType: DataSourceType): Single<UserCourseReviewsResult> =
        zip(
            courseListUserInteractor.getAllUserCourses(UserCourseQuery(page = 1), sourceType = primaryDataSourceType),
            fetchUserCourseReviews(primaryDataSourceType)
        ).flatMap { (userCourses, courseReviews) ->
            val courseReviewsDistinct = courseReviews.distinctBy { it.course }
            val userCoursesIds = userCourses.map(UserCourse::course)
            val courseWithReviewsIds = courseReviewsDistinct.map(CourseReview::course)
            val coursesWithoutReviews = (userCoursesIds - courseWithReviewsIds).toSet()
            courseRepository
                .getCourses(userCoursesIds, primarySourceType = primaryDataSourceType)
                .flatMap { courses ->
                    val progresses = courses
                        .filter { course -> coursesWithoutReviews.contains(course.id) }
                        .mapNotNull { it.progress }
                    val coursesById = courses.associateBy { it.id }
                    val coursesByProgress = courses
                        .filter { it.progress != null }
                        .associateBy { it.progress!! }

                    progressRepository
                        .getProgresses(progresses, primarySourceType = primaryDataSourceType)
                        .map { resultProgresses ->
                            val potentialReviewItems =
                                resolvePotentialReviewItems(resultProgresses, coursesByProgress)

                            val reviewedItems =
                                courseReviewsDistinct.map { UserCourseReviewItem.ReviewedItem(course = coursesById.getValue(it.course), courseReview = it) }

                            val potentialHeader = if (potentialReviewItems.isNotEmpty()) {
                                listOf(UserCourseReviewItem.PotentialReviewHeader(potentialReviewCount = potentialReviewItems.size))
                            } else {
                                emptyList()
                            }

                            val reviewHeader = if (reviewedItems.isNotEmpty()) {
                                listOf(UserCourseReviewItem.ReviewedHeader(reviewedCount = reviewedItems.size))
                            } else {
                                emptyList()
                            }

                            UserCourseReviewsResult(
                                potentialHeader + potentialReviewItems + reviewHeader + reviewedItems,
                                potentialHeader,
                                potentialReviewItems,
                                reviewHeader,
                                reviewedItems
                            )
                        }
                }
        }

    fun removeCourseReview(courseReview: CourseReview): Completable =
        courseReviewsRepository.removeCourseReview(courseReview.id)

    fun enrolledCourse(course: Course): Maybe<UserCourseReviewItem> =
        zip(
            fetchReviewEnrolled(course),
            fetchProgressEnrolled(listOf(course))
        ).flatMapMaybe { (courseReviews, progresses) ->
            val courseReview = courseReviews.firstOrNull()
            val progress = progresses.firstOrNull()

            when {
                courseReview != null ->
                    Maybe.just(UserCourseReviewItem.ReviewedItem(course, courseReview))

                progress != null -> {
                    if (progress.nStepsPassed * 100 safeDiv progress.nSteps > 80) {
                        Maybe.just(UserCourseReviewItem.PotentialReviewItem(course))
                    } else {
                        Maybe.empty()
                    }
                }

                else ->
                    Maybe.empty()
            }
        }

    fun getAnalyticProfileData(): Single<ProfileData> =
        Single
            .fromCallable { userPreferences.userId }
            .flatMap { user ->  userRepository.getUser(user, primarySourceType = DataSourceType.CACHE).toSingle() }
            .map { user -> ProfileData(user, user.id == userPreferences.userId) }

    private fun fetchReviewEnrolled(course: Course): Single<List<CourseReview>> =
        profileRepository
            .getProfile()
            .flatMapMaybe { profile -> courseReviewsRepository.getCourseReviewByCourseIdAndUserId(course.id, profile.id, DataSourceType.REMOTE) }
            .map { listOf(it) }
            .toSingle()
            .onErrorReturnItem(emptyList())

    private fun fetchProgressEnrolled(courses: List<Course>): Single<List<Progress>> =
        progressRepository
            .getProgresses(courses.getProgresses())
            .onErrorReturnItem(emptyList())

    private fun resolvePotentialReviewItems(resultProgresses: List<Progress>, coursesByProgress: Map<String, Course>): List<UserCourseReviewItem.PotentialReviewItem> =
        resultProgresses.mapNotNull {
            val canWriteReview = it.nStepsPassed * 100 safeDiv it.nSteps > 80
            if (canWriteReview) {
                UserCourseReviewItem.PotentialReviewItem(coursesByProgress.getValue(it.id))
            } else {
                null
            }
        }

    private fun fetchUserCourseReviews(primaryDataSourceType: DataSourceType): Single<List<CourseReview>> =
        profileRepository
            .getProfile(primarySourceType = primaryDataSourceType)
            .flatMap { profile -> getAllCourseReviews(profile.id, primaryDataSourceType) }

    private fun getAllCourseReviews(userId: Long, primaryDataSourceType: DataSourceType): Single<List<CourseReview>> =
        Observable.range(1, Int.MAX_VALUE)
            .concatMapSingle { page ->
                courseReviewsRepository.getCourseReviewsByUserId(
                    userId,
                    page,
                    sourceType = primaryDataSourceType
                )
            }
            .takeUntil { !it.hasNext }
            .reduce(emptyList()) { a, b -> a + b }
}