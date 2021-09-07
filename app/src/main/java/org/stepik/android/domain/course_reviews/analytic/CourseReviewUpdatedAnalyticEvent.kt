package org.stepik.android.domain.course_reviews.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class CourseReviewUpdatedAnalyticEvent(
    fromRating: Int,
    toRating: Int,
    courseId: Long,
    source: String
) : AnalyticEvent {
    companion object {
        private const val PARAM_FROM_RATING = "from_rating"
        private const val PARAM_TO_RATING = "to_rating"
        private const val PARAM_COURSE = "course"
        private const val PARAM_SOURCE = "source"
    }
    override val name: String =
        "Course review updated"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_FROM_RATING to fromRating,
            PARAM_TO_RATING to toRating,
            PARAM_COURSE to courseId,
            PARAM_SOURCE to source
        )
}