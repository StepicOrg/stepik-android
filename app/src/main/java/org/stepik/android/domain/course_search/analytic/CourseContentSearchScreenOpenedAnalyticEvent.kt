package org.stepik.android.domain.course_search.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class CourseContentSearchScreenOpenedAnalyticEvent(
    courseId: Long,
    courseTitle: String
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
    }
    override val name: String =
        "Course content search screen opened"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to courseId,
            PARAM_TITLE to courseTitle
        )
}