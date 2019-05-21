package org.stepik.android.data.course.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.Course

interface CourseCacheDataSource {
    fun getCourses(vararg ids: Long): Single<List<Course>>
    fun saveCourses(courses: List<Course>): Completable

    fun saveCourse(course: Course): Completable =
        saveCourses(listOf(course))

    fun removeCourse(courseId: Long): Completable
}