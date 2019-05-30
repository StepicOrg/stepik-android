package org.stepik.android.remote.course.source

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.web.Api
import org.stepik.android.remote.course.model.CourseResponse
import org.stepik.android.data.course.source.CourseRemoteDataSource
import org.stepik.android.model.Course
import org.stepik.android.remote.base.chunkedSingleMap
import javax.inject.Inject

class CourseRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : CourseRemoteDataSource {
    private val courseResponseMapper =
        Function<CourseResponse, List<Course>>(CourseResponse::courses)

    override fun getCourses(vararg courseIds: Long): Single<List<Course>> =
        courseIds
            .chunkedSingleMap { ids ->
                api.getCoursesReactive(ids)
                    .map(courseResponseMapper)
            }
}