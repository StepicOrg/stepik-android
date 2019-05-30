package org.stepik.android.domain.course.repository

import io.reactivex.Single
import org.stepik.android.model.CourseReviewSummary

interface CourseReviewSummaryRepository {
    fun getCourseReviewSummary(courseReviewSummaryId: Long): Single<CourseReviewSummary>
}