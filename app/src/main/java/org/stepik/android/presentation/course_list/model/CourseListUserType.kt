package org.stepik.android.presentation.course_list.model

import org.stepik.android.domain.course_list.model.CourseListUserQuery

enum class CourseListUserType(val courseListUserQuery: CourseListUserQuery) {
    ALL(CourseListUserQuery(page = 1)),
    FAVORITE(CourseListUserQuery(page = 1, isFavorite = true)),
    ARCHIVED(CourseListUserQuery(page = 1, isArchived = true))
}