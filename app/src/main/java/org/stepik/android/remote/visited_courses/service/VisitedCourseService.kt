package org.stepik.android.remote.visited_courses.service

import io.reactivex.Single
import org.stepik.android.remote.visited_courses.model.VisitedCoursesResponse
import retrofit2.http.GET

interface VisitedCourseService {
    @GET("api/visited-courses")
    fun getVisitedCourses(): Single<VisitedCoursesResponse>
}