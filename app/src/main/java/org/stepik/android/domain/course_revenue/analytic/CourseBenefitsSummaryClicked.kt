package org.stepik.android.domain.course_revenue.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent

class CourseBenefitsSummaryClicked(
    courseId: Long,
    courseTitle: String?,
    isExpanded: Boolean
) : AnalyticEvent {
    companion object {
        private const val PARAM_COURSE = "course"
        private const val PARAM_COURSE_TITLE = "course_title"
        private const val PARAM_IS_EXPANDED = "is_expanded"
    }

    override val name: String =
        "Course benefits summary clicked"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_COURSE to courseId,
            PARAM_COURSE_TITLE to courseTitle.orEmpty(),
            PARAM_IS_EXPANDED to isExpanded
        )
}