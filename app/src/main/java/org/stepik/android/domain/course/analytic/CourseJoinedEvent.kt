package org.stepik.android.domain.course.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.model.Course

class CourseJoinedEvent(
    source: String,
    course: Course,
    isWishlisted: Boolean
) : AnalyticEvent {
    companion object {
        private const val PARAM_SOURCE = "source"
        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
        private const val PARAM_IS_WISHLISTED = "is_wishlisted"

        const val SOURCE_PREVIEW = "preview"
    }

    override val name: String =
        "Course joined"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_SOURCE to source,
            PARAM_COURSE to course.id,
            PARAM_TITLE to course.title.toString(),
            PARAM_IS_WISHLISTED to isWishlisted
        )
}