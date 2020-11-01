package org.stepik.android.domain.visited_courses.repository

import io.reactivex.Completable
import io.reactivex.Flowable
import org.stepik.android.domain.visited_courses.model.VisitedCourse

interface VisitedCoursesRepository {
    fun observeVisitedCourses(): Flowable<List<VisitedCourse>>
    fun saveVisitedCourse(courseId: Long): Completable
    fun removedVisitedCourses(): Completable
}