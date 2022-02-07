package org.stepik.android.data.user_courses.source

import io.reactivex.Single
import ru.nobird.app.core.model.PagedList
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.user_courses.model.UserCourse

interface UserCoursesRemoteDataSource {
    fun getUserCourses(userCourseQuery: UserCourseQuery): Single<PagedList<UserCourse>>
    fun saveUserCourse(userCourseId: Long, userCourse: UserCourse): Single<UserCourse>
}