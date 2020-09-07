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
        private const val PARAM_PLATFORM = "platform"
        private const val PARAM_POSITION = "position"
        private const val PARAM_DATA = "data"

        private const val PLATFORM_VALUE = "android"
        private const val POSITION_VALUE = 1
    }

    override val name: String =
        "catalog-display"

    override val params: Map<String, Any> =
        mapOf(
            DATA to mapOf(
                PARAM_COURSE to courseId,
                PARAM_SOURCE to source.name,
                PARAM_PLATFORM to PLATFORM_VALUE,
                PARAM_POSITION to POSITION_VALUE,
                PARAM_DATA to source.params
            )
        )

    override val sources: EnumSet<AnalyticSource> =
        EnumSet.of(AnalyticSource.STEPIK_API)
}