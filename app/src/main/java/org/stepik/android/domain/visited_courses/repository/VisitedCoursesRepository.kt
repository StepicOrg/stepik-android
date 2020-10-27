package org.stepik.android.domain.visited_courses.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.visited_courses.model.VisitedCourse

interface VisitedCoursesRepository {
    fun getVisitedCourses(primarySourceType: DataSourceType = DataSourceType.CACHE): Single<List<VisitedCourse>>
    fun saveVisitedCourse(courseId: Long): Completable
}