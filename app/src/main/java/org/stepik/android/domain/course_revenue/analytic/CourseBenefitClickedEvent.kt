package org.stepik.android.domain.course_revenue.analytic

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.course_revenue.model.CourseBenefit
import java.util.Locale

class CourseBenefitClickedEvent(
    benefitId: Long,
    benefitStatus: CourseBenefit.Status,
    courseId: Long,
    courseTitle: String?
) : AnalyticEvent {
    companion object {
        private const val PARAM_BENEFIT = "benefit"
        private const val PARAM_STATUS = "status"
        private const val PARAM_COURSE = "course"
        private const val PARAM_COURSE_TITLE = "course_title"
    }

    override val name: String =
        "Course benefit clicked"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_BENEFIT to benefitId,
            PARAM_STATUS to benefitStatus.name.toLowerCase(Locale.ROOT),
            PARAM_COURSE to courseId,
            PARAM_COURSE_TITLE to courseTitle.orEmpty()
        )
}