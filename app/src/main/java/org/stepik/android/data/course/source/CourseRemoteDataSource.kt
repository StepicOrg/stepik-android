package org.stepik.android.data.course.source

import io.reactivex.Single
import org.stepik.android.model.Course

interface CourseRemoteDataSource {
    fun getCourses(vararg courseIds: Long): Single<List<Course>>
}