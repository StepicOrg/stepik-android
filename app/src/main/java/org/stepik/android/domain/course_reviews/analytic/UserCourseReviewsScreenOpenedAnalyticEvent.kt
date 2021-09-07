package org.stepik.android.domain.course_reviews.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class UserCourseReviewsScreenOpenedAnalyticEvent(
    state: String,
    id: Long
) : AnalyticEvent {
    companion object {
        private const val PARAM_STATE = "state"
        private const val PARAM_ID = "id"
    }
    override val name: String =
        "User course reviews screen opened"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_STATE to state,
            PARAM_ID to id
        )
}