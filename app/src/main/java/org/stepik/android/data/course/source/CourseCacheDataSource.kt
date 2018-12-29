package org.stepik.android.data.course.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.Course

interface CourseCacheDataSource {
    fun getCourses(vararg ids: Long): Single<List<Course>>
    fun saveCourse(course: Course): Completable
    fun removeCourse(courseId: Long): Completable
}