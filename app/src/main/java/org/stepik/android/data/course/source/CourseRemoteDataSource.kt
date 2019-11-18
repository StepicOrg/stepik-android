package org.stepik.android.data.course.source

import io.reactivex.Single
import org.stepik.android.model.Course
import org.stepik.android.remote.course.model.CourseResponse
import org.stepik.android.remote.course.model.UserCoursesResponse

interface CourseRemoteDataSource {
    fun getCoursesReactive(page: Int, vararg courseIds: Long): Single<CourseResponse>
    fun getCoursesReactive(vararg courseIds: Long): Single<List<Course>>

    fun getUserCourses(page: Int): Single<UserCoursesResponse>
    fun getPopularCourses(page: Int): Single<CourseResponse>
}