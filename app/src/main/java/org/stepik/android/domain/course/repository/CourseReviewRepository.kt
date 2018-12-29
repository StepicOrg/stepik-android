package org.stepik.android.domain.course.repository

import io.reactivex.Single
import org.stepic.droid.model.CourseReviewSummary

interface CourseReviewRepository {
    fun getCourseReview(courseReviewId: Long): Single<CourseReviewSummary>
}