package org.stepik.android.data.course.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.CourseReviewSummary

interface CourseReviewSummaryCacheDataSource {
    fun getCourseReviewSummaries(vararg courseReviewSummaryIds: Long): Single<List<CourseReviewSummary>>
    fun saveCourseReviewSummaries(courseReviewSummaries: List<CourseReviewSummary>): Completable
}