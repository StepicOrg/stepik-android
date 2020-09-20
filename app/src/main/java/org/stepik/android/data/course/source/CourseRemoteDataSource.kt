package org.stepik.android.data.course.source

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.model.Course

interface CourseRemoteDataSource {
    fun getCourses(courseIds: List<Long>): Single<List<Course>>
    fun getCourses(courseListQuery: CourseListQuery): Single<PagedList<Course>>
}