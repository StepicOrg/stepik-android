package org.stepik.android.domain.personal_deadlines.analytic

import kotlinx.android.parcel.Parcelize
import org.stepik.android.domain.base.analytic.ParcelableAnalyticEvent

@Parcelize
class DeadlinesNotificationClicked(
    val course: Long,
    val hours: Long
) : ParcelableAnalyticEvent {
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