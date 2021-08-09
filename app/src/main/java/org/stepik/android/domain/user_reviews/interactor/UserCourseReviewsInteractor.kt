package org.stepik.android.domain.user_reviews.interactor

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.repository.CourseRepository
import org.stepik.android.domain.course_list.interactor.CourseListUserInteractor
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.course_reviews.repository.CourseReviewsRepository
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.domain.user_reviews.model.UserCourseReviewItem
import org.stepik.android.model.Course
import org.stepik.android.model.Progress
import org.stepik.android.view.injection.user_reviews.LearningActionsScope
import javax.inject.Inject

@LearningActionsScope
class UserCourseReviewsInteractor
@Inject
constructor(
    private val courseRepository: CourseRepository,
    private val courseReviewsRepository: CourseReviewsRepository,
    private val courseListUserInteractor: CourseListUserInteractor,
    private val profileRepository: ProfileRepository,
    private val progressRepository: ProgressRepository
) {

    fun fetchUserCourseReviewItems(primaryDataSourceType: DataSourceType): Single<List<UserCourseReviewItem>> =
        zip(
            courseListUserInteractor.getUserCoursesShared(),
            fetchUserCourseReviews(primaryDataSourceType)
        ).flatMap { (userCourses, courseReviews) ->
            val userCoursesIds = userCourses.map(UserCourse::course)
            val courseWithReviewsIds = courseReviews.map(CourseReview::course)
            val coursesIdsToFetch = userCoursesIds - courseWithReviewsIds
            courseRepository
                .getCourses(coursesIdsToFetch, primarySourceType = primaryDataSourceType)
                .flatMap { courses ->
                    val progresses = courses.mapNotNull { it.progress }
                    val coursesByProgress = courses
                        .filter { it.progress != null }
                        .associateBy { it.progress!! }

                    progressRepository
                        .getProgresses(progresses, primarySourceType = primaryDataSourceType)
                        .map { resultProgresses ->
                            val reviewedItems =
                                courseReviews.map { UserCourseReviewItem.ReviewedItem(it) }
                            val potentialReviewItems =
                                resolvePotentialReviewItems(resultProgresses, coursesByProgress)
                            listOf(UserCourseReviewItem.PotentialReviewHeader(potentialReviewCount = potentialReviewItems.size)) +
                                    potentialReviewItems +
                                    listOf(UserCourseReviewItem.ReviewedHeader(reviewedCount = reviewedItems.size)) + reviewedItems
                        }
                }
        }

    private fun resolvePotentialReviewItems(resultProgresses: List<Progress>, coursesByProgress: Map<String, Course>): List<UserCourseReviewItem.PotentialReviewItem> =
        resultProgresses.mapNotNull {
            val canWriteReview = it.nStepsPassed * 100 / it.nSteps > 80
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
            .concatMapSingle {
                courseReviewsRepository.getCourseReviewsByUserId(
                    userId,
                    sourceType = primaryDataSourceType
                )
            }
            .takeUntil { !it.hasNext }
            .reduce(emptyList()) { a, b -> a + b }
}