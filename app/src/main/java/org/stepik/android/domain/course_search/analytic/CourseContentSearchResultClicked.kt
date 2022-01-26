package org.stepik.android.domain.course_search.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import ru.nobird.app.core.model.mapOfNotNull

class CourseContentSearchResultClicked(
    val courseId: Long,
    val courseTitle: String,
    val query: String,
    val suggestion: String?,
    val type: String,
    val stepId: Long?
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
        private const val PARAM_QUERY = "query"
        private const val PARAM_SUGGESTION = "suggestion"
        private const val PARAM_TYPE = "type"
        private const val PARAM_STEP = "step"
    }
    override val name: String =
        "Course content search result clicked"

    override val params: Map<String, Any> =
        mapOfNotNull(
            PARAM_COURSE to courseId,
            PARAM_TITLE to courseTitle,
            PARAM_QUERY to query,
            PARAM_SUGGESTION to suggestion,
            PARAM_TYPE to type,
            PARAM_STEP to stepId
        )
}