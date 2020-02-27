package org.stepik.android.domain.course_list.model

import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.model.Course
import ru.nobird.android.core.model.Identifiable

data class CourseListItem(
    val course: Course,
    val courseStats: CourseStats
) : Identifiable<Long> {
    override val id: Long
        get() = course.id
}