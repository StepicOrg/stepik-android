package org.stepik.android.domain.course_reviews.interactor

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Maybes
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.maybeFirst
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.repository.CourseReviewSummaryRepository
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.course_reviews.model.CourseReviewItem
import org.stepik.android.domain.course_reviews.repository.CourseReviewsRepository
import org.stepik.android.domain.profile.repository.ProfileRepository
import org.stepik.android.domain.progress.repository.ProgressRepository
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.Course
import org.stepik.android.model.user.User
import javax.inject.Inject

class CourseReviewsInteractor
@Inject
constructor(
    private val courseObservableSource: Observable<Course>,

    private val profileRepository: ProfileRepository,
    private val progressRepository: ProgressRepository,
    private val courseReviewsRepository: CourseReviewsRepository,
    private val courseReviewSummaryRepository: CourseReviewSummaryRepository,
    private val userRepository: UserRepository
) {
    fun getCourseUpdates(): Observable<Course> =
        courseObservableSource
            .skip(1)

    fun getCourseReviewItems(courseId: Long, page: Int = 1, sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<CourseReviewItem>> =
        profileRepository
            .getProfile(sourceType)
            .flatMap { profile ->
                getCourseReviewItems(profile.id, courseId, page, sourceType)
            }

    private fun getCourseReviewItems(
        profileId: Long,
        courseId: Long,
        page: Int,
        sourceType: DataSourceType
    ): Single<PagedList<CourseReviewItem>> =
        courseReviewsRepository
            .getCourseReviewsByCourseId(courseId, page, sourceType)
            .flatMap { courseReviews ->
                val userIds = courseReviews
                    .map(CourseReview::user)
                    .filter { it != profileId }
                    .distinct()
                    .toLongArray()

                val currentUserReviewSource =
                    if (page == 1) {
                        zip(
                            getCourseReviewSummary(courseReviews.isNotEmpty(), sourceType),
                            resolveCurrentUserCourseReview(profileId, courseId, courseReviews.isNotEmpty(), sourceType)
                        ) { a, b -> a + b }
                            .onErrorReturnItem(emptyList())
                    } else {
                        Single.just(emptyList())
                    }

                zip(currentUserReviewSource, Single.just(courseReviews), userRepository.getUsers(userIds = *userIds))
            }
            .map { (currentUserReview, courseReviews, users) ->
                val usersMap = users
                    .associateBy(User::id)

                val courseReviewsItems = courseReviews
                    .mapNotNull { review ->
                        val user = usersMap[review.user]
                            ?: return@mapNotNull null

                        CourseReviewItem.Data(review, user, isCurrentUserReview = false) as CourseReviewItem
                    }

                PagedList(currentUserReview + courseReviewsItems, page = courseReviews.page, hasNext = courseReviews.hasNext, hasPrev = courseReviews.hasPrev)
            }

    /**
     * Returns current user course review wrapped in list monad
     */
    fun resolveCurrentUserCourseReview(profileId: Long, courseId: Long, hasReviews: Boolean, sourceType: DataSourceType = DataSourceType.CACHE): Single<List<CourseReviewItem>> =
        Maybes
            .zip(
                courseReviewsRepository.getCourseReviewByCourseIdAndUserId(courseId, profileId, sourceType),
                userRepository.getUsers(profileId, primarySourceType = sourceType).maybeFirst()
            )
            .map { (review, user) ->
                listOf<CourseReviewItem>(CourseReviewItem.Data(review, user, isCurrentUserReview = true))
            }
            .switchIfEmpty(resolveCourseReviewBanner(hasReviews))

    private fun resolveCourseReviewBanner(hasReviews: Boolean): Single<List<CourseReviewItem>> =
        courseObservableSource
            .firstOrError()
            .flatMap { course ->
                val progressId = course.progress
                if (progressId == null) {
                    Single.just(emptyList())
                } else {
                    progressRepository
                        .getProgress(progressId)
                        .map { progress ->
                            val canWriteReview = progress.nStepsPassed * 100 / progress.nSteps > 80
                            listOf(CourseReviewItem.ComposeBanner(canWriteReview, isReviewsEmpty = !hasReviews))
                        }
                }
            }

    private fun getCourseReviewSummary(hasReviews: Boolean, sourceType: DataSourceType = DataSourceType.CACHE): Single<List<CourseReviewItem>> =
        if (hasReviews) {
            courseObservableSource
                .firstOrError()
                .flatMap { course ->
                    courseReviewSummaryRepository
                        .getCourseReviewSummary(course.reviewSummary, sourceType)
                        .map { listOf(CourseReviewItem.Summary(it)) }
                        .toSingle(emptyList())
                }
        } else {
            Single.just(emptyList())
        }
}