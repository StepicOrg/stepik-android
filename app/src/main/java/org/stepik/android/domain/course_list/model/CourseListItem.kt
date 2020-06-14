package org.stepik.android.domain.course_list.model

import org.stepik.android.domain.course.analytic.CourseViewSource
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.model.Course
import ru.nobird.android.core.model.Identifiable

sealed class CourseListItem {
    data class Data(
        val course: Course,
        val courseStats: CourseStats,
        val isAdaptive: Boolean,
        val source: CourseViewSource
    ) : CourseListItem(), Identifiable<Long> {
        override val id: Long
            get() = course.id
    }

    class PlaceHolder(val courseId: Long = -1) : CourseListItem()
}