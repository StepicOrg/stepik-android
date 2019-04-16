package org.stepik.android.domain.course_reviews.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_reviews.model.CourseReview

interface CourseReviewsRepository {
    /**
     * Returns [page] of items if exists from [sourceType]
     */
    fun getCourseReviewsByCourseId(courseId: Long, page: Int = 1, sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<CourseReview>>

    fun createCourseReview(courseReview: CourseReview): Completable

    fun saveCourseReview(courseReview: CourseReview): Completable

    fun removeCourseReview(courseReviewId: Long): Completable
}