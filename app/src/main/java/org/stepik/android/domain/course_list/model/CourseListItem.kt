package org.stepik.android.domain.course_list.model

import org.stepic.droid.model.CollectionDescriptionColors
import org.stepik.android.domain.course.model.CourseStats
import org.stepik.android.model.Course
import ru.nobird.android.core.model.Identifiable

sealed class CourseListItem {
    data class Data(
        val course: Course,
        val courseStats: CourseStats
    ) : CourseListItem(), Identifiable<Long> {
        override val id: Long
            get() = course.id
    }

    object PlaceHolder : CourseListItem()

    data class PlaceHolderText(val text: String, val colors: CollectionDescriptionColors) : CourseListItem()
}