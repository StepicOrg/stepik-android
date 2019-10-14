package org.stepik.android.data.course.repository

import io.reactivex.Single
import org.stepic.droid.util.doCompletableOnSuccess
import org.stepik.android.data.course.source.CourseReviewSummaryCacheDataSource
import org.stepik.android.data.course.source.CourseReviewSummaryRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course.repository.CourseReviewSummaryRepository
import org.stepik.android.model.CourseReviewSummary
import javax.inject.Inject

class CourseReviewSummaryRepositoryImpl
@Inject
constructor(
    private val courseReviewSummaryRemoteDataSource: CourseReviewSummaryRemoteDataSource,
    private val courseReviewSummaryCacheDataSource: CourseReviewSummaryCacheDataSource
) : CourseReviewSummaryRepository {
    override fun getCourseReviewSummaries(vararg courseReviewSummaryIds: Long, sourceType: DataSourceType): Single<List<CourseReviewSummary>> =
        when (sourceType) {
            DataSourceType.CACHE ->
                courseReviewSummaryCacheDataSource
                    .getCourseReviewSummaries(*courseReviewSummaryIds)

            DataSourceType.REMOTE ->
                courseReviewSummaryRemoteDataSource
                    .getCourseReviewSummaries(*courseReviewSummaryIds)
                    .doCompletableOnSuccess(courseReviewSummaryCacheDataSource::saveCourseReviewSummaries)

            else -> throw IllegalArgumentException("Unsupported source type = $sourceType")
        }
}