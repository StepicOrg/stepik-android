package org.stepik.android.remote.course_list

import io.reactivex.Single
import org.stepik.android.data.course_list.source.CourseListRemoteDataSource
import org.stepik.android.remote.course_list.model.CourseCollectionsResponse
import org.stepik.android.remote.course_list.service.CourseListService
import javax.inject.Inject

class CourseListRemoteDataSourceImpl
@Inject
constructor(
    private val courseListService: CourseListService
) : CourseListRemoteDataSource {
    override fun getCourseCollections(language: String): Single<CourseCollectionsResponse> =
        courseListService.getCourseLists(language)
}