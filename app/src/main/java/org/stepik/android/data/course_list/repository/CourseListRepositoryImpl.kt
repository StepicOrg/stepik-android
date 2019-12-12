package org.stepik.android.data.course_list.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.CourseListType
import org.stepic.droid.util.PagedList
import org.stepik.android.data.course_list.source.CourseListCacheDataSource
import org.stepik.android.data.course_list.source.CourseListRemoteDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.model.CourseListQuery
import org.stepik.android.domain.course_list.repository.CourseListRepository
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseListRepositoryImpl
@Inject
constructor(
    private val courseListCacheDataSource: CourseListCacheDataSource,
    private val courseListRemoteDataSource: CourseListRemoteDataSource
) : CourseListRepository {
    override fun getCourseList(courseListType: CourseListType, page: Int, lang: String, sourceType: DataSourceType): Single<PagedList<Course>> =
        when (sourceType) {
            DataSourceType.REMOTE ->
                if (courseListType == CourseListType.FEATURED) {
                    courseListRemoteDataSource.getPopularCourses(page, lang)
                } else {
                    throw IllegalArgumentException("Unsupported course list type = $courseListType for source = $sourceType")
                }

            DataSourceType.CACHE ->
                courseListCacheDataSource.getCourseList(courseListType)
        }

    override fun getCourseList(courseListQuery: CourseListQuery): Single<PagedList<Course>> =
        courseListRemoteDataSource.getCourseList(courseListQuery)

    override fun addCourseToList(courseListType: CourseListType, courseId: Long): Completable =
        courseListCacheDataSource.addCourseToList(courseListType, courseId)

    override fun removeCourseFromList(courseListType: CourseListType, courseId: Long): Completable =
        courseListCacheDataSource.removeCourseFromList(courseListType, courseId)
}