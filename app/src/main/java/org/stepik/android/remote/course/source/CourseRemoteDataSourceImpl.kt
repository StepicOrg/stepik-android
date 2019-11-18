package org.stepik.android.remote.course.source

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepik.android.data.course.source.CourseRemoteDataSource
import org.stepik.android.model.Course
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.course.model.CourseResponse
import org.stepik.android.remote.course.service.CourseService
import javax.inject.Inject

class CourseRemoteDataSourceImpl
@Inject
constructor(
    private val courseService: CourseService
) : CourseRemoteDataSource {
    private val courseResponseMapper =
        Function<CourseResponse, List<Course>>(CourseResponse::courses)

    override fun getCoursesReactive(page: Int, vararg courseIds: Long): Single<CourseResponse> {
        val ids = if (courseIds.isEmpty()) {
            longArrayOf(0)
        } else {
            courseIds
        }
        return courseService.getCoursesReactive(page, ids)
    }

    override fun getCoursesReactive(vararg courseIds: Long): Single<List<Course>> =
        courseIds
            .chunkedSingleMap { ids ->
                courseService.getCoursesReactive(ids)
                    .map(courseResponseMapper)
            }
}