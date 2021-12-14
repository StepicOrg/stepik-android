package org.stepik.android.domain.course_purchase.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class RestoreCoursePurchaseSuccessAnalyticEvent(
    courseId: Long
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
    }
    override val name: String =
        "Restore course purchase success"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to courseId
        )
}