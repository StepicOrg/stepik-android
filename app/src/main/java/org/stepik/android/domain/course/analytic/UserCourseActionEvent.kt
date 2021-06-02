package org.stepik.android.domain.course.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class UserCourseActionEvent(
    action: String,
    courseId: Long,
    title: String,
    isPaid: Boolean,
    source: CourseViewSource
) : AnalyticEvent {
    companion object {
        private const val PARAM_ACTION = "action"
        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
        private const val PARAM_IS_PAID = "is_paid"
        private const val PARAM_SOURCE = "source"
    }

    override val name: String =
        "User course action"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_ACTION to action,
            PARAM_COURSE to courseId,
            PARAM_TITLE to title,
            PARAM_IS_PAID to isPaid
        ) + source.params.mapKeys { "${PARAM_SOURCE}_${it.key}" }
}