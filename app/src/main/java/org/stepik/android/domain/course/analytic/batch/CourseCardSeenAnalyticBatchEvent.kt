package org.stepik.android.domain.course.analytic.batch

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.base.analytic.AnalyticSource
import org.stepik.android.domain.course.analytic.CourseViewSource
import java.util.EnumSet

class CourseCardSeenAnalyticBatchEvent(
    courseId: Long,
    source: CourseViewSource
) : AnalyticEvent {
    companion object {
        private const val DATA = "data"

        private const val PARAM_COURSE = "course"
        private const val PARAM_SOURCE = "source"
    }

    override val name: String =
        "Course card seen"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_SOURCE to source.name,
            DATA to mapOf(
                PARAM_COURSE to courseId
            ) + source.params
        )

    override val sources: EnumSet<AnalyticSource> =
        EnumSet.of(AnalyticSource.STEPIK_API)
}