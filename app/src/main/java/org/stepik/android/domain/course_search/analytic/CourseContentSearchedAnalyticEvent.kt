package org.stepik.android.domain.course_search.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import ru.nobird.android.core.model.mapOfNotNull

class CourseContentSearchedAnalyticEvent(
    val courseId: Long,
    val courseTitle: String,
    val query: String,
    val suggestion: String? = null
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
        private const val PARAM_QUERY = "query"
        private const val PARAM_SUGGESTION = "suggestion"
    }

    override val name: String =
        "Course content searched"

    override val params: Map<String, Any> =
        mapOfNotNull(
            PARAM_COURSE to courseId,
            PARAM_TITLE to courseTitle,
            PARAM_QUERY to query,
            PARAM_SUGGESTION to suggestion
        )
}