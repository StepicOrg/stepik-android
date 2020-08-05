package org.stepik.android.domain.course.analytic.batch

import org.stepik.android.domain.base.analytic.AnalyticEvent
import org.stepik.android.domain.base.analytic.AnalyticSource
import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.model.Course
import java.util.EnumSet

class CoursePreviewScreenOpenedAnalyticBatchEvent(
    course: Course,
    source: CourseViewSource
) : AnalyticEvent {
    companion object {
        private const val DATA = "data"

        private const val PARAM_COURSE = "course"
        private const val PARAM_SOURCE = "source"
    }

    override val name: String =
        "catalog-click"

    override val params: Map<String, Any> =
        mapOf(
            PARAM_SOURCE to source.name,
            DATA to mapOf(
                PARAM_COURSE to course.id
            ) + source.params
        )

    override val sources: EnumSet<AnalyticSource> =
        EnumSet.of(AnalyticSource.STEPIK_API)
}