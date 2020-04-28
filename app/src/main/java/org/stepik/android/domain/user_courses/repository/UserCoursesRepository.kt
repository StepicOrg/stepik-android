package org.stepik.android.domain.user_courses.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.user_courses.model.UserCourse

interface UserCoursesRepository {
    fun getUserCourses(page: Int = 1, sourceType: DataSourceType = DataSourceType.CACHE): Single<PagedList<UserCourse>>
    fun saveUserCourse(userCourse: UserCourse): Single<PagedList<UserCourse>>

    /***
     *  Cached purpose only
     */
    fun getUserCourse(courseId: Long): Single<UserCourse>
    fun addUserCourse(userCourse: UserCourse): Completable
    fun removeUserCourse(courseId: Long): Completable
}