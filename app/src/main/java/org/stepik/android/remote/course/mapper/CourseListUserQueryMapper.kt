package org.stepik.android.remote.course.mapper

import org.stepic.droid.util.putNullable
import org.stepik.android.domain.course_list.model.CourseListUserQuery
import javax.inject.Inject

class CourseListUserQueryMapper
@Inject
constructor() {
    companion object {
        private const val PAGE = "page"
        private const val IS_FAVORITE = "is_favorite"
        private const val IS_ARCHIVED = "is_archived"
    }

    fun mapToQueryMap(courseListUserQuery: CourseListUserQuery): Map<String, String> {
        val mutableMap = hashMapOf<String, String>()

        mutableMap.putNullable(PAGE, courseListUserQuery.page?.toString())
        mutableMap.putNullable(IS_FAVORITE, courseListUserQuery.isFavorite?.toString())
        mutableMap.putNullable(IS_ARCHIVED, courseListUserQuery.isArchived?.toString())

        return mutableMap
    }
}