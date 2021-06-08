package org.stepik.android.domain.course_continue.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.model.Course
import org.stepik.android.presentation.course_continue.model.CourseContinueInteractionSource

class CourseContinuePressedEvent(
    course: Course,
    source: CourseContinueInteractionSource,
    viewSource: CourseViewSource
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
        private const val PARAM_SOURCE = "source"
        private const val PARAM_VIEW_SOURCE = "view_source"
    }

    override val name: String =
        "Continue course pressed"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to course.id,
            PARAM_TITLE to course.title.toString(),
            PARAM_SOURCE to source.source,
            PARAM_VIEW_SOURCE to viewSource.name
        ) + viewSource.params.mapKeys { "${PARAM_VIEW_SOURCE}_${it.key}" }
}