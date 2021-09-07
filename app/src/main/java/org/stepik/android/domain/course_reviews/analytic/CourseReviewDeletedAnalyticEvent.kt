package org.stepik.android.domain.course_reviews.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class CourseReviewDeletedAnalyticEvent(
    rating: Int,
    courseId: Long,
    source: String
) : AnalyticEvent {
    companion object {
        private const val PARAM_RATING = "rating"
        private const val PARAM_COURSE = "course"
        private const val PARAM_SOURCE = "source"
    }
    override val name: String =
        "Course review deleted"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_RATING to rating,
            PARAM_COURSE to courseId,
            PARAM_SOURCE to source
        )
}