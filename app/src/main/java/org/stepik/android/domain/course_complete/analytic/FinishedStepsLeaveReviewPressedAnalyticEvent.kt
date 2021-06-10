package org.stepik.android.domain.course_complete.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.model.Course

class FinishedStepsLeaveReviewPressedAnalyticEvent(
    course: Course,
    completeRate: Float
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_TITLE = "title"
        private const val COMPLETE_RATE = "complete_rate"
    }
    override val name: String =
        "Finished steps leave review pressed"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to course.id,
            PARAM_TITLE to course.title.toString(),
            COMPLETE_RATE to completeRate
        )
}