package org.stepik.android.data.course.source

import io.reactivex.Single
import org.stepik.android.model.CourseReviewSummary

interface CourseReviewSummaryRemoteDataSource {
    fun getCourseReviewSummaries(vararg courseReviewSummaryIds: Long): Single<List<CourseReviewSummary>>
}