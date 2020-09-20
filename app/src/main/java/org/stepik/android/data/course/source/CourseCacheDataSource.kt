package org.stepik.android.data.course.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.model.Course

interface CourseCacheDataSource {
    fun getCourses(ids: List<Long>): Single<List<Course>>
    fun saveCourses(courses: List<Course>): Completable

    fun removeCourse(courseId: Long): Completable

    fun removeCachedCourses(): Completable
}