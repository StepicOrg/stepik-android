package org.stepik.android.domain.course_reviews.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class EditCourseReviewPressedAnalyticEvent(
    courseId: Long,
    source: String
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_SOURCE = "source"
    }
    override val name: String =
        "Edit course review pressed"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to courseId,
            PARAM_SOURCE to source
        )
}