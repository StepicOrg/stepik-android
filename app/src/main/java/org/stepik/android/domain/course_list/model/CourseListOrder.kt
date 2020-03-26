package org.stepik.android.domain.course_list.model

enum class CourseListOrder(val order: String) {
    ORDER_ACTIVITY_DESC("-activity"),
    ORDER_POPULARITY_DESC("-popularity")
}