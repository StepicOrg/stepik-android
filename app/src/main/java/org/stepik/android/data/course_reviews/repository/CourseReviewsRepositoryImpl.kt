package org.stepik.android.data.course_reviews.repository

import io.reactivex.Single
import org.stepik.android.data.course_reviews.source.CourseReviewsRemoteDataSource
import org.stepik.android.domain.course_reviews.model.CourseReview
import org.stepik.android.domain.course_reviews.repository.CourseReviewsRepository
import javax.inject.Inject

class CourseReviewsRepositoryImpl
@Inject
constructor(
    private val courseReviewsRemoteDataSource: CourseReviewsRemoteDataSource
) : CourseReviewsRepository {
    override fun getCourseReviewsByCourseId(courseId: Long): Single<List<CourseReview>> =
        courseReviewsRemoteDataSource.getCourseReviewsByCourseId(courseId)
}