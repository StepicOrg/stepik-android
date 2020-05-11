package org.stepik.android.data.user_courses.source

import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_list.model.CourseListUserQuery
import org.stepik.android.domain.user_courses.model.UserCourse

interface UserCoursesRemoteDataSource {
    fun getUserCourses(courseListUserQuery: CourseListUserQuery): Single<PagedList<UserCourse>>
    fun getUserCourseByCourseId(courseId: Long): Maybe<UserCourse>
    fun saveUserCourse(userCourseId: Long, userCourse: UserCourse): Single<UserCourse>
}