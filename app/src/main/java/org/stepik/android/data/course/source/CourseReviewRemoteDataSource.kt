package org.stepik.android.data.course.source

import io.reactivex.Single
import org.stepic.droid.model.CourseReviewSummary

interface CourseReviewRemoteDataSource {
    fun getCourseReviews(vararg courseReviewIds: Long): Single<List<CourseReviewSummary>>
}