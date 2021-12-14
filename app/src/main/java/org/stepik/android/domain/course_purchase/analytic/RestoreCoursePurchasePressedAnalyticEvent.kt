package org.stepik.android.domain.course_purchase.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.model.Course

class RestoreCoursePurchasePressedAnalyticEvent(
    course: Course
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
    }
    override val name: String =
        "Restore course purchase pressed"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to course.id,
            PARAM_TITLE to course.title.toString()
        )
}