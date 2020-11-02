package org.stepik.android.data.visited_courses.source

import io.reactivex.Single
import org.stepik.android.domain.visited_courses.model.VisitedCourse

interface VisitedCoursesRemoteDataSource {
    fun getVisitedCourses(): Single<List<VisitedCourse>>
}