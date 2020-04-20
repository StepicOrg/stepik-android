package org.stepik.android.domain.course_list.model

sealed class UserCoursesLoaded {
    object Empty : UserCoursesLoaded()
    class FirstCourse(val courseListItem: CourseListItem.Data) : UserCoursesLoaded()
}