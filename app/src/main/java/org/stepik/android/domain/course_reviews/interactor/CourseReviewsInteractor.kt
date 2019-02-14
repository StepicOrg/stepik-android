package org.stepik.android.domain.course_reviews.interactor

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.Singles.zip
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.course_reviews.model.CourseReviewItem
import org.stepik.android.domain.course_reviews.repository.CourseReviewsRepository
import org.stepik.android.domain.user.repository.UserRepository
import org.stepik.android.model.Course
import org.stepik.android.model.user.User
import javax.inject.Inject

class CourseReviewsInteractor
@Inject
constructor(
    private val courseObservableSource: Observable<Course>,

    private val courseReviewsRepository: CourseReviewsRepository,
    private val userRepository: UserRepository
) {
    fun getCourseUpdates(): Observable<Course> =
        courseObservableSource
            .skip(1)

    fun getCourseReviewItems(courseId: Long, page: Int = 1, sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<CourseReviewItem.Data>> =
        courseReviewsRepository
            .getCourseReviewsByCourseId(courseId, page, sourceType)
            .flatMap { courseReviews ->
                val userIds = courseReviews
                    .map(CourseReview::user)
                    .distinct()
                    .toLongArray()

                zip(Single.just(courseReviews), userRepository.getUsers(userIds = *userIds))
            }
            .map { (courseReviews, users) ->
                val usersMap = users
                    .associateBy(User::id)

                val courseReviewsItems = courseReviews
                    .mapNotNull { review ->
                        val user = usersMap[review.user]
                            ?: return@mapNotNull null

                        CourseReviewItem.Data(review, user)
                    }

                PagedList(courseReviewsItems, page = courseReviews.page, hasNext = courseReviews.hasNext, hasPrev = courseReviews.hasPrev)
            }
}