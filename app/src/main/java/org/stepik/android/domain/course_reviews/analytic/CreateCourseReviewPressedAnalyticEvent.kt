package org.stepik.android.domain.course_reviews.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class CreateCourseReviewPressedAnalyticEvent(
    courseId: Long,
    title: String,
    source: String
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
        private const val PARAM_SOURCE = "source"
    }
    override val name: String =
        "Create course review pressed"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to courseId,
            PARAM_TITLE to title,
            PARAM_SOURCE to source
        )
}