package org.stepik.android.remote.course_reviews

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import ru.nobird.android.domain.rx.maybeFirst
import org.stepik.android.data.course_reviews.source.CourseReviewsRemoteDataSource
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.course_reviews.model.CourseReviewRequest
import org.stepik.android.remote.course_reviews.model.CourseReviewsResponse
import org.stepik.android.remote.course_reviews.service.CourseReviewService
import javax.inject.Inject

class CourseReviewsRemoteDataSourceImpl
@Inject
constructor(
    private val courseReviewService: CourseReviewService
) : CourseReviewsRemoteDataSource {
    override fun getCourseReviewsByCourseId(courseId: Long, page: Int): Single<PagedList<CourseReview>> =
        courseReviewService
            .getCourseReviewsByCourseId(courseId, page)
            .map { it.toPagedList(CourseReviewsResponse::courseReviews) }

    override fun getCourseReviewByCourseIdAndUserId(courseId: Long, userId: Long): Maybe<CourseReview> =
        courseReviewService
            .getCourseReviewByCourseIdAndUserId(courseId, userId)
            .map(CourseReviewsResponse::courseReviews)
            .maybeFirst()

    override fun createCourseReview(courseReview: CourseReview): Single<CourseReview> =
        courseReviewService
            .createCourseReview(CourseReviewRequest(courseReview))
            .map { it.courseReviews.first() }

    override fun saveCourseReview(courseReview: CourseReview): Single<CourseReview> =
        courseReviewService
            .updateCourseReview(courseReview.id, CourseReviewRequest(courseReview))
            .map { it.courseReviews.first() }

    override fun removeCourseReview(courseReviewId: Long): Completable =
        courseReviewService
            .removeCourseReview(courseReviewId)
}