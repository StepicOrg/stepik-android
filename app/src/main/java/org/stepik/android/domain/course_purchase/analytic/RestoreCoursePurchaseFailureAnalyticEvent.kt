package org.stepik.android.domain.course_purchase.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class RestoreCoursePurchaseFailureAnalyticEvent(
    courseId: Long,
    type: String,
    throwable: Throwable
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_TYPE = "type"
        private const val PARAM_MESSAGE = "message"
        private const val PARAM_STACKTRACE = "stacktrace"
    }
    override val name: String =
        "Restore course purchase failure"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to courseId,
            PARAM_TYPE to type,
            PARAM_MESSAGE to throwable.message.toString(),
            PARAM_STACKTRACE to throwable.stackTraceToString()
        )
}