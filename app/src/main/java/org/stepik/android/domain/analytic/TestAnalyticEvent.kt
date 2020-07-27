package org.stepik.android.domain.analytic

import org.stepic.droid.util.DateTimeHelper
import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.base.analytic.AnalyticSource
import org.stepik.android.domain.course.analytic.CourseViewSource
import java.util.EnumSet

class TestAnalyticEvent(
    courseId: Long,
    source: CourseViewSource
) : AnalyticEvent {
    companion object {
        private const val TIMESTAMP = "timestamp"
        private const val DATA = "data"

        private const val PARAM_COURSE = "course"
        private const val PARAM_SOURCE = "source"
    }
    override val name: String
        get() = "TestEvent"

    override val params: Map<String, Any> =
        mapOf(
            TIMESTAMP to DateTimeHelper.nowUtc(),
            PARAM_SOURCE to source.name,
            DATA to mapOf<String, Any>(
                PARAM_COURSE to courseId
            )
        )

    override val sources: EnumSet<AnalyticSource>
        get() = EnumSet.of(AnalyticSource.STEPIK_API)
}