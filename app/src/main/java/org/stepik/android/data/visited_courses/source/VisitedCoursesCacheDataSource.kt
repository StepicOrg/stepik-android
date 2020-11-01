package org.stepik.android.data.visited_courses.source

import io.reactivex.Completable
import io.reactivex.Flowable
import org.stepik.android.domain.visited_courses.model.VisitedCourse

interface VisitedCoursesCacheDataSource {
    fun getVisitedCourses(): Flowable<List<VisitedCourse>>
    fun saveVisitedCourses(visitedCourses: List<VisitedCourse>)
    fun saveVisitedCourse(courseId: Long): Completable
    fun removeVisitedCourses(): Completable
}