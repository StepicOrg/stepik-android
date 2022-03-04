package org.stepik.android.domain.personal_deadlines.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class DeadlinesNotificationClicked(
    course: Long,
    hours: Long
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_HOURS = "hours"
    }

    override val name: String =
        "Personal deadlines app notification clicked"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to course,
            PARAM_HOURS to hours
        )
}