package org.stepik.android.domain.course_revenue.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class CourseBenefitsScreenOpenedEvent(
    courseId: Long,
    courseTitle: String?
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_COURSE_TITLE = "course_title"
    }

    override val name: String =
        "Course benefits screen opened"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to courseId,
            PARAM_COURSE_TITLE to courseTitle.orEmpty()
        )
}