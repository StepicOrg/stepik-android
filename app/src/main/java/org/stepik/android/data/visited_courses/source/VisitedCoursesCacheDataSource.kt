package org.stepik.android.data.visited_courses.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.visited_courses.model.VisitedCourse

interface VisitedCoursesCacheDataSource {
    fun getVisitedCourses(): Single<List<VisitedCourse>>
    fun saveVisitedCourses(visitedCourses: List<VisitedCourse>): Completable
    fun saveVisitedCourse(courseId: Long): Completable
}