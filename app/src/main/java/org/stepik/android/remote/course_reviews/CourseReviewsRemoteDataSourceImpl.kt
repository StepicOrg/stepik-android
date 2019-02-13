package org.stepik.android.remote.course_reviews

import io.reactivex.Single
import org.stepic.droid.web.StepicRestLoggedService
import org.stepik.android.data.course_reviews.source.CourseReviewsRemoteDataSource
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.remote.course_reviews.model.CourseReviewsResponse
import javax.inject.Inject

class CourseReviewsRemoteDataSourceImpl
@Inject
constructor(
    private val stepicRestLoggedService: StepicRestLoggedService
) : CourseReviewsRemoteDataSource {
    override fun getCourseReviewsByCourseId(courseId: Long): Single<List<CourseReview>> =
        stepicRestLoggedService
            .getCourseReviewsByCourseId(courseId)
            .map(CourseReviewsResponse::courseReviews)
}