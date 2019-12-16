package org.stepik.android.remote.course_list.mapper

import org.stepic.droid.util.putNullable
import org.stepik.android.domain.course_list.model.CourseListQuery
import javax.inject.Inject

class CourseListQueryMapper
@Inject
constructor() {
    companion object {
        private const val PAGE = "page"
        private const val ORDER = "order"
        private const val TEACHER = "teacher"
        private const val IS_PUBLIC = "is_public"
        private const val IS_EXLUDE_ENDED = "exclude_ended"
    }

    fun mapToQueryMap(courseListQuery: CourseListQuery): Map<String, String> {
        val mutableMap = hashMapOf<String, String>()

        mutableMap.putNullable(PAGE, courseListQuery.page?.toString())
        mutableMap.putNullable(ORDER, courseListQuery.order)
        mutableMap.putNullable(TEACHER, courseListQuery.teacher?.toString())
        mutableMap.putNullable(IS_PUBLIC, courseListQuery.isPublic?.toString())
        mutableMap.putNullable(IS_EXLUDE_ENDED, courseListQuery.isExcludeEnded?.toString())

        return mutableMap
    }
}