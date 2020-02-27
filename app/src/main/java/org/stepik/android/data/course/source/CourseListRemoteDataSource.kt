package org.stepik.android.data.course.source

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.model.Course

interface CourseListRemoteDataSource {
    fun getPopularCourses(page: Int, lang: String): Single<PagedList<Course>>

    fun getCourseList(courseListQuery: CourseListQuery): Single<PagedList<Course>>
}