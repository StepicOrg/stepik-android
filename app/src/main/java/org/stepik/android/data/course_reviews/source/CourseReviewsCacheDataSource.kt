package org.stepik.android.data.course_reviews.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_reviews.model.CourseReview

interface CourseReviewsCacheDataSource {
    fun getCourseReviewsByCourseId(courseId: Long): Single<List<CourseReview>>
    fun saveCourseReviews(courseReviews: List<CourseReview>): Completable
}