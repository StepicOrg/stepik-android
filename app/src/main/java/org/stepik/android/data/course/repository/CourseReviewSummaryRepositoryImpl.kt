package org.stepik.android.data.course.repository

import io.reactivex.Single
import org.stepik.android.model.CourseReviewSummary
import org.stepik.android.data.course.source.CourseReviewSummaryRemoteDataSource
import org.stepik.android.domain.course.repository.CourseReviewSummaryRepository
import javax.inject.Inject

class CourseReviewSummaryRepositoryImpl
@Inject
constructor(
    private val courseReviewSummaryRemoteDataSource: CourseReviewSummaryRemoteDataSource
) : CourseReviewSummaryRepository {
    override fun getCourseReviewSummary(courseReviewSummaryId: Long): Single<CourseReviewSummary> =
        courseReviewSummaryRemoteDataSource
            .getCourseReviewSummaries(courseReviewSummaryId)
            .map { it.first() }

    override fun getCourseReviewSummaries(courseReviewSummaryIds: LongArray): Single<List<CourseReviewSummary>> =
        courseReviewSummaryRemoteDataSource
            .getCourseReviewSummaries(*courseReviewSummaryIds)
}