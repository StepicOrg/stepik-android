package org.stepik.android.data.course.repository

import io.reactivex.Single
import org.stepic.droid.model.CourseReviewSummary
import org.stepik.android.data.course.source.CourseReviewRemoteDataSource
import org.stepik.android.domain.course.repository.CourseReviewRepository
import javax.inject.Inject

class CourseReviewRepositoryImpl
@Inject
constructor(
    private val courseReviewRemoteDataSource: CourseReviewRemoteDataSource
) : CourseReviewRepository {
    override fun getCourseReview(courseReviewId: Long): Single<CourseReviewSummary> =
            courseReviewRemoteDataSource
                .getCourseReviews(courseReviewId)
                .map { it.first() }
}