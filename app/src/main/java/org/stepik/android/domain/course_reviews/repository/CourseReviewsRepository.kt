package org.stepik.android.domain.course_reviews.repository

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_reviews.model.CourseReview

interface CourseReviewsRepository {
    fun getCourseReviewsByCourseId(courseId: Long): Single<PagedList<CourseReview>>
}