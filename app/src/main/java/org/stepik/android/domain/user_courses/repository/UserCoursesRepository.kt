package org.stepik.android.domain.user_courses.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.CourseListUserQuery
import org.stepik.android.domain.user_courses.model.UserCourse

interface UserCoursesRepository {
    fun getUserCourses(courseListUserQuery: CourseListUserQuery = CourseListUserQuery(page = 1), sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<UserCourse>>
    fun getUserCourseByCourseId(courseId: Long): Maybe<UserCourse>
    fun saveUserCourse(userCourse: UserCourse): Single<UserCourse>

    /***
     *  Cached purpose only
     */
    fun addUserCourse(userCourse: UserCourse): Completable
    fun removeUserCourse(courseId: Long): Completable
}