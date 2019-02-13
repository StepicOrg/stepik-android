package org.stepik.android.data.course_reviews.source

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_reviews.model.CourseReview

interface CourseReviewsRemoteDataSource {
    fun getCourseReviewsByCourseId(courseId: Long): Single<PagedList<CourseReview>>
}