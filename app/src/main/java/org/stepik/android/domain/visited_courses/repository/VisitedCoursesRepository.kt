package org.stepik.android.domain.visited_courses.repository

import io.reactivex.Completable
import io.reactivex.Observable
import org.stepik.android.domain.visited_courses.model.VisitedCourse

interface VisitedCoursesRepository {
    fun observeVisitedCourses(): Observable<List<VisitedCourse>>
    fun saveVisitedCourse(courseId: Long): Completable
}