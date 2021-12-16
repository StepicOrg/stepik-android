package org.stepik.android.domain.course_purchase.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.model.Course

class BuyCoursePromoStartPressedAnalyticEvent(
    course: Course
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
    }
    override val name: String =
        "Buy course promo start pressed"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to course.id
        )
}