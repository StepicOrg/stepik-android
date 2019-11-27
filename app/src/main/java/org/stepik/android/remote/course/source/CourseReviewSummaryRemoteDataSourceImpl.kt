package org.stepik.android.remote.course.source

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.course.source.CourseReviewSummaryRemoteDataSource
import org.stepik.android.model.CourseReviewSummary
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.course.model.CourseReviewSummaryResponse
import org.stepik.android.remote.course.service.CourseReviewSummaryService
import javax.inject.Inject

class CourseReviewSummaryRemoteDataSourceImpl
@Inject
constructor(
    private val courseReviewSummaryService: CourseReviewSummaryService
) : CourseReviewSummaryRemoteDataSource {
    private val courseReviewSummaryResponseMapper =
        Function<CourseReviewSummaryResponse, List<CourseReviewSummary>>(CourseReviewSummaryResponse::courseReviewSummaries)

    override fun getCourseReviewSummaries(vararg courseReviewSummaryIds: Long): Single<List<CourseReviewSummary>> =
        courseReviewSummaryIds
            .chunkedSingleMap { ids ->
                courseReviewSummaryService.getCourseReviewSummaries(ids)
                    .map(courseReviewSummaryResponseMapper)
            }
}