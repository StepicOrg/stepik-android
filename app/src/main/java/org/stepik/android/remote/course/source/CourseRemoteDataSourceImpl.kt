package org.stepik.android.remote.course.source

import io.reactivex.Single
import org.stepic.droid.web.Api
import org.stepik.android.data.course.source.CourseRemoteDataSource
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseRemoteDataSourceImpl
@Inject
constructor(
    private val api: Api
) : CourseRemoteDataSource {
    override fun getCourses(vararg ids: Long): Single<List<Course>> =
        api.getCoursesReactive(1, ids)
            .map { it.courses?.filterNotNull() ?: emptyList() }
}