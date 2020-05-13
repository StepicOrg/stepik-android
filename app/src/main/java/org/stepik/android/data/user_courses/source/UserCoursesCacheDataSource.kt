package org.stepik.android.data.user_courses.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.course_list.model.UserCourseQuery
import org.stepik.android.domain.user_courses.model.UserCourse

interface UserCoursesCacheDataSource {
    fun getUserCourses(userCourseQuery: UserCourseQuery): Single<List<UserCourse>>
    fun getUserCourse(courseId: Long): Single<UserCourse>
    fun saveUserCourses(userCourses: List<UserCourse>): Completable
    fun removeUserCourse(courseId: Long): Completable
}