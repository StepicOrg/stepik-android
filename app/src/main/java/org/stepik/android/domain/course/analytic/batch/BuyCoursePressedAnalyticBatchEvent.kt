package org.stepik.android.domain.course.analytic.batch

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.base.analytic.AnalyticSource
import java.util.EnumSet

class BuyCoursePressedAnalyticBatchEvent(
    courseId: Long
) : AnalyticEvent {
    companion object {
        private const val DATA = "data"

        private const val PARAM_COURSE = "course"
    }

    override val name: String =
        "buy-course-pressed"

    override val params: Map<String, Any> =
        mapOf(
            DATA to mapOf(
                PARAM_COURSE to courseId
            )
        )

    override val sources: EnumSet<AnalyticSource> =
        EnumSet.of(AnalyticSource.STEPIK_API)
}