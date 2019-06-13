package org.stepik.android.domain.course.repository

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.maybeFirst
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.model.CourseReviewSummary

interface CourseReviewSummaryRepository {
    fun getCourseReviewSummary(courseReviewSummaryId: Long, sourceType: DataSourceType = DataSourceType.CACHE): Maybe<CourseReviewSummary> =
        getCourseReviewSummaries(courseReviewSummaryId, sourceType = sourceType).maybeFirst()

    fun getCourseReviewSummaries(vararg courseReviewSummaryIds: Long, sourceType: DataSourceType = DataSourceType.CACHE): Single<List<CourseReviewSummary>>
}