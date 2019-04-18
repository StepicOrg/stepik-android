package org.stepik.android.remote.course_reviews

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepic.droid.util.maybeFirst
import org.stepic.droid.web.StepicRestLoggedService
import org.stepik.android.data.course_reviews.source.CourseReviewsRemoteDataSource
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.course_reviews.model.CourseReviewRequest
import org.stepik.android.remote.course_reviews.model.CourseReviewsResponse
import javax.inject.Inject

class CourseReviewsRemoteDataSourceImpl
@Inject
constructor(
    private val stepicRestLoggedService: StepicRestLoggedService
) : CourseReviewsRemoteDataSource {
    override fun getCourseReviewsByCourseId(courseId: Long, page: Int): Single<PagedList<CourseReview>> =
        stepicRestLoggedService
            .getCourseReviewsByCourseId(courseId, page)
            .map { it.toPagedList(CourseReviewsResponse::courseReviews) }

    override fun getCourseReviewByCourseIdAndUserId(courseId: Long, userId: Long): Maybe<CourseReview> =
        stepicRestLoggedService
            .getCourseReviewByCourseIdAndUserId(courseId, userId)
            .map(CourseReviewsResponse::courseReviews)
            .maybeFirst()

    override fun createCourseReview(courseReview: CourseReview): Single<CourseReview> =
        stepicRestLoggedService
            .createCourseReview(CourseReviewRequest(courseReview))
            .map { it.courseReviews.first() }

    override fun saveCourseReview(courseReview: CourseReview): Single<CourseReview> =
        stepicRestLoggedService
            .updateCourseReview(courseReview.id, CourseReviewRequest(courseReview))
            .map { it.courseReviews.first() }

    override fun removeCourseReview(courseReviewId: Long): Completable =
        stepicRestLoggedService
            .removeCourseReview(courseReviewId)
}