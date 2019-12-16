package org.stepik.android.domain.course_list.model

data class CourseListQuery(
    val page: Int? = null,
    val order: String? = null,
    val teacher: Long? = null,

    val isExcludeEnded: Boolean? = null,
    val isPublic: Boolean? = null
) {
    companion object {
        const val ORDER_ACTIVITY_DESC = "-activity"
        const val ORDER_POPULARITY_DESC = "-popularity"
    }
}