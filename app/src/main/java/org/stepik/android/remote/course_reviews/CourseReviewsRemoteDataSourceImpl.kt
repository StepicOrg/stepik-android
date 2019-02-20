package org.stepik.android.remote.course_reviews

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepic.droid.web.StepicRestLoggedService
import org.stepik.android.data.course_reviews.source.CourseReviewsRemoteDataSource
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.remote.base.mapper.toPagedList
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
}