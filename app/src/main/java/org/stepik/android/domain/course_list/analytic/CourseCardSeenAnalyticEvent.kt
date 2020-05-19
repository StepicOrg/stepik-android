package org.stepik.android.domain.course_list.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class CourseViewAnalyticEvent(
    courseId: Long
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
    }

    override val name: String =
        "Course card seen"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to courseId
        )
}