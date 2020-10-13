package org.stepik.android.remote.user_courses.mapper

import org.stepik.android.domain.course_list.model.UserCourseQuery
import ru.nobird.android.core.model.mapOfNotNull
import javax.inject.Inject

class UserCourseQueryMapper
@Inject
constructor() {
    companion object {
        private const val PAGE = "page"
        private const val IS_FAVORITE = "is_favorite"
        private const val IS_ARCHIVED = "is_archived"
        private const val COURSE = "course"
    }

    fun mapToQueryMap(userCourseQuery: UserCourseQuery): Map<String, String> =
        mapOfNotNull(
            PAGE to userCourseQuery.page?.toString(),
            IS_FAVORITE to userCourseQuery.isFavorite?.toString(),
            IS_ARCHIVED to userCourseQuery.isArchived?.toString(),
            COURSE to userCourseQuery.course?.toString()
        )
}