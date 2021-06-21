package org.stepik.android.domain.course.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.model.Course

class BuyCoursePressedEvent(
    course: Course,
    source: String,
    isWishlisted: Boolean
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_SOURCE = "source"
        private const val PARAM_IS_WISHLISTED = "is_wishlisted"

        const val HOME_WIDGET = "home_widget"
        const val COURSE_SCREEN = "course_screen"
    }

    override val name: String =
        "Buy course pressed"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to course.id,
            PARAM_SOURCE to source,
            PARAM_IS_WISHLISTED to isWishlisted
        )
}