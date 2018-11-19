package org.stepik.android.remote.course.source

import io.reactivex.Single
import org.stepic.droid.model.CourseReviewSummary
import org.stepic.droid.web.Api
import org.stepic.droid.web.CourseReviewResponse
import org.stepik.android.data.course.source.CourseReviewRemoteDataSource
import javax.inject.Inject

class CourseReviewRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : CourseReviewRemoteDataSource {
    override fun getCourseReviews(vararg courseReviewIds: Long): Single<List<CourseReviewSummary>> =
            api.getCourseReviews(courseReviewIds).map(CourseReviewResponse::courseReviewSummaries)
}