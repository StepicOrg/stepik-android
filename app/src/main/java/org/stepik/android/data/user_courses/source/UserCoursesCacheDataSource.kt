package org.stepik.android.data.user_courses.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.UserCourse

interface UserCoursesCacheDataSource {
    fun getUserCourses(): Single<List<UserCourse>>
    fun saveUserCourses(userCourses: List<UserCourse>): Completable
    fun removeUserCourse(courseId: Long): Completable
}