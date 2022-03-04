package org.stepik.android.domain.personal_deadlines.analytic

import org.stepik.android.domain.base.analytic.BundleableAnalyticEvent

data class DeadlinesNotificationDismissed(
    val course: Long,
    val hours: Long
) : BundleableAnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_HOURS = "hours"
    }

    override val name: String =
        "Personal deadlines app notification dismissed"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to course,
            PARAM_HOURS to hours
        )
}