package org.stepik.android.domain.course_list.model

import org.stepik.android.model.Course

sealed class UserCoursesLoaded {
    object Empty : UserCoursesLoaded()
    class FirstCourse(val course: Course) : UserCoursesLoaded()
}