package org.stepik.android.remote.course_list

import io.reactivex.Single
import org.stepic.droid.util.PagedList
import org.stepik.android.data.course_list.source.CourseListRemoteDataSource
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.model.Course
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.course.model.CourseResponse
import org.stepik.android.remote.course_list.mapper.CourseListQueryMapper
import org.stepik.android.remote.course_list.service.CourseListService
import javax.inject.Inject

class CourseListRemoteDataSourceImpl
@Inject
constructor(
    private val courseListService: CourseListService,
    private val courseListQueryMapper: CourseListQueryMapper
) : CourseListRemoteDataSource {
    override fun getPopularCourses(page: Int, lang: String): Single<PagedList<Course>> =
        courseListService
            .getPopularCourses(page, lang)
            .map { it.toPagedList(CourseResponse::courses) }

    override fun getCourseList(courseListQuery: CourseListQuery): Single<PagedList<Course>> =
        courseListService
            .getCourses(courseListQueryMapper.mapToQueryMap(courseListQuery))
            .map { it.toPagedList(CourseResponse::courses) }
}