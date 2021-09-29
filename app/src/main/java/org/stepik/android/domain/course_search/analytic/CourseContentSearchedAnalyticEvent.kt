package org.stepik.android.domain.course_search.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class CourseContentSearchedAnalyticEvent(
    val courseId: Long,
    val courseTitle: String,
    val query: String,
    val suggestion: Boolean
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
        private const val PARAM_QUERY = "query"
        private const val PARAM_SUGGESTION = "suggestions"
    }

    override val name: String =
        "Course content searched"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to courseId,
            PARAM_TITLE to courseTitle,
            PARAM_QUERY to query,
            PARAM_SUGGESTION to suggestion
        )
}