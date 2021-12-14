package org.stepik.android.domain.course_purchase.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.model.Course

class BuyCourseVerificationFailureAnalyticEvent(
    course: Course,
    throwable: Throwable
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
        private const val PARAM_ERROR_MESSAGE = "error_message"
    }
    override val name: String =
        "Buy course verification failure"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to course.id,
            PARAM_TITLE to course.title.toString(),
            PARAM_ERROR_MESSAGE to throwable.message.toString()
        )
}