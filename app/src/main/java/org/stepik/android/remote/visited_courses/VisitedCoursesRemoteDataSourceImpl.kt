package org.stepik.android.remote.visited_courses

import io.reactivex.Single
import org.stepik.android.data.visited_courses.source.VisitedCoursesRemoteDataSource
import org.stepik.android.domain.visited_courses.model.VisitedCourse
import org.stepik.android.remote.visited_courses.model.VisitedCoursesResponse
import org.stepik.android.remote.visited_courses.service.VisitedCourseService
import javax.inject.Inject

class VisitedCoursesRemoteDataSourceImpl
@Inject
constructor(
    private val visitedCourseService: VisitedCourseService
) : VisitedCoursesRemoteDataSource {
    override fun getVisitedCourses(): Single<List<VisitedCourse>> =
        visitedCourseService
            .getVisitedCourses()
            .map(VisitedCoursesResponse::visitedCourses)
}