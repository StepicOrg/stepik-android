package org.stepik.android.domain.user_courses.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.user_courses.model.UserCourse

interface UserCoursesRepository {
    fun getUserCourses(userCourseQuery: UserCourseQuery = UserCourseQuery(page = 1, isArchived = false), sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<UserCourse>>
    fun getUserCourseByCourseId(courseId: Long, sourceType: DataSourceType = DataSourceType.CACHE): Maybe<UserCourse>
    fun saveUserCourse(userCourse: UserCourse): Single<UserCourse>

    /***
     *  Cached purpose only
     */
    fun addUserCourse(userCourse: UserCourse): Completable
    fun removeUserCourse(courseId: Long): Completable
}