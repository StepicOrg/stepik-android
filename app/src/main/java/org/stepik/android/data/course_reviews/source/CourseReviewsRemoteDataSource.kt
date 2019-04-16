package org.stepik.android.data.course_reviews.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_reviews.model.CourseReview

interface CourseReviewsRemoteDataSource {
    fun getCourseReviewsByCourseId(courseId: Long, page: Int = 1): Single<PagedList<CourseReview>>

    fun createCourseReview(courseReview: CourseReview): Single<CourseReview>
    fun saveCourseReview(courseReview: CourseReview): Single<CourseReview>
    fun removeCourseReview(courseReviewId: Long): Completable
}