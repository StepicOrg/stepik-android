package org.stepik.android.data.course_reviews.source

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_reviews.model.CourseReview

interface CourseReviewsCacheDataSource {
    fun getCourseReviewsByCourseId(courseId: Long): Single<PagedList<CourseReview>>
    fun getCourseReviewByCourseIdAndUserId(courseId: Long, userId: Long): Maybe<CourseReview>

    fun saveCourseReview(courseReview: CourseReview): Completable =
        saveCourseReviews(listOf(courseReview))

    fun saveCourseReviews(courseReviews: List<CourseReview>): Completable

    fun removeCourseReview(courseReviewId: Long): Completable
}