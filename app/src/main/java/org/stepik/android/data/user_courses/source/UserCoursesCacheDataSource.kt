package org.stepik.android.data.user_courses.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.user_courses.model.UserCourse
import org.stepik.android.presentation.course_list.model.CourseListUserType

interface UserCoursesCacheDataSource {
    fun getUserCourses(courseListUserType: CourseListUserType = CourseListUserType.ALL): Single<List<UserCourse>>
    fun getUserCourse(courseId: Long): Single<UserCourse>
    fun saveUserCourses(userCourses: List<UserCourse>): Completable
    fun removeUserCourse(courseId: Long): Completable
}