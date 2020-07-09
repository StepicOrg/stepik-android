package org.stepik.android.remote.course.mapper

import org.stepik.android.domain.course_list.model.CourseListQuery
import ru.nobird.android.core.model.putNullable
import javax.inject.Inject

class CourseListQueryMapper
@Inject
constructor() {
    companion object {
        private const val PAGE = "page"
        private const val ORDER = "order"
        private const val TEACHER = "teacher"
        private const val LANGUAGE = "language"
        private const val IS_PUBLIC = "is_public"
        private const val IS_EXCLUDE_ENDED = "exclude_ended"
        private const val IS_CATALOGED = "is_cataloged"
    }

    fun mapToQueryMap(courseListQuery: CourseListQuery): Map<String, String> {
        val mutableMap = hashMapOf<String, String>()

        mutableMap.putNullable(PAGE, courseListQuery.page?.toString())
        mutableMap.putNullable(ORDER, courseListQuery.order?.order)
        mutableMap.putNullable(TEACHER, courseListQuery.teacher?.toString())
        mutableMap.putNullable(LANGUAGE, courseListQuery.language)
        mutableMap.putNullable(IS_PUBLIC, courseListQuery.isPublic?.toString())
        mutableMap.putNullable(IS_EXCLUDE_ENDED, courseListQuery.isExcludeEnded?.toString())
        mutableMap.putNullable(IS_CATALOGED, courseListQuery.isCataloged?.toString())

        return mutableMap
    }
}