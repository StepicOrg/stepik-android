package org.stepik.android.domain.course_purchase.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class RestoreCoursePurchasePressedAnalyticEvent(
    courseId: Long,
    restoreCoursePurchaseSource: String
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_SOURCE = "source"
    }
    override val name: String =
        "Restore course purchase pressed"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to courseId,
            PARAM_SOURCE to restoreCoursePurchaseSource
        )
}