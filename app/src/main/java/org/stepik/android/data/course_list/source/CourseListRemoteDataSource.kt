package org.stepik.android.data.course_list.source

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.model.Course

interface CourseListRemoteDataSource {
    fun getPopularCourses(page: Int, lang: String): Single<PagedList<Course>>
}