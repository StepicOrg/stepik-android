package org.stepik.android.remote.course.source

import io.reactivex.Single
import io.reactivex.functions.Function
import org.stepic.droid.util.PagedList
import org.stepik.android.data.course.source.CourseRemoteDataSource
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.model.Course
import org.stepik.android.remote.base.chunkedSingleMap
import org.stepik.android.remote.base.mapper.toPagedList
import org.stepik.android.remote.course.mapper.CourseListQueryMapper
import org.stepik.android.remote.course.model.CourseResponse
import org.stepik.android.remote.course.service.CourseService
import javax.inject.Inject

class CourseRemoteDataSourceImpl
@Inject
constructor(
    private val courseService: CourseService,
    private val courseListQueryMapper: CourseListQueryMapper
) : CourseRemoteDataSource {
    private val courseResponseMapper =
        Function<CourseResponse, List<Course>>(CourseResponse::courses)

    override fun getCourses(courseIds: List<Long>): Single<List<Course>> =
        courseIds
            .chunkedSingleMap { ids ->
                courseService.getCourses(ids)
                    .map(courseResponseMapper)
            }

    override fun getCourses(courseListQuery: CourseListQuery): Single<PagedList<Course>> =
        courseService
            .getCourses(courseListQueryMapper.mapToQueryMap(courseListQuery))
            .map { it.toPagedList(CourseResponse::courses) }
}