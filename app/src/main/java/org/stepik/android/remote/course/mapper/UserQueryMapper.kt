package org.stepik.android.remote.course.mapper

import org.stepic.droid.util.putNullable
import org.stepik.android.domain.course_list.model.UserCourseQuery
import javax.inject.Inject

class UserQueryMapper
@Inject
constructor() {
    companion object {
        private const val PAGE = "page"
        private const val IS_FAVORITE = "is_favorite"
        private const val IS_ARCHIVED = "is_archived"
    }

    fun mapToQueryMap(userCourseQuery: UserCourseQuery): Map<String, String> {
        val mutableMap = hashMapOf<String, String>()

        mutableMap.putNullable(PAGE, userCourseQuery.page?.toString())
        mutableMap.putNullable(IS_FAVORITE, userCourseQuery.isFavorite?.toString())
        mutableMap.putNullable(IS_ARCHIVED, userCourseQuery.isArchived?.toString())

        return mutableMap
    }
}