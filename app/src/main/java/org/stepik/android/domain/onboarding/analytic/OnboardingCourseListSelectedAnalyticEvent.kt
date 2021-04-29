package org.stepik.android.domain.onboarding.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class OnboardingCourseListSelectedAnalyticEvent(
    courseListTitle: String,
    courseListId: Long
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE_LIST_TITLE = "course_list_title"
        private const val PARAM_COURSE_LIST_ID = "course_list_id"
    }
    override val name: String =
        "Onboarding course list selected"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE_LIST_TITLE to courseListTitle,
            PARAM_COURSE_LIST_ID to courseListId
        )
}