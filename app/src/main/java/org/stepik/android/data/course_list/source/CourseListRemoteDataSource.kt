package org.stepik.android.data.course_list.source

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.model.Course
import org.stepik.android.remote.course_list.model.CourseCollectionsResponse

interface CourseListRemoteDataSource {
    fun getCourseCollections(language: String): Single<CourseCollectionsResponse>
    fun getPopularCourses(page: Int, lang: String): Single<PagedList<Course>>
}