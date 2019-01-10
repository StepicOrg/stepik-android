package org.stepik.android.data.course_list.repository

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.CourseListType
import org.stepik.android.data.course_list.source.CourseListCacheDataSource
import org.stepik.android.domain.base.DataSourceType
import org.stepik.android.domain.course_list.repository.CourseListRepository
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseListRepositoryImpl
@Inject
constructor(
    private val courseListCacheDataSource: CourseListCacheDataSource
) : CourseListRepository {
    override fun getCourseList(courseListType: CourseListType, primarySourceType: DataSourceType): Single<List<Course>> {
        if (primarySourceType != DataSourceType.CACHE) {
            throw IllegalArgumentException("Unsupported source type = $primarySourceType")
        }

        return courseListCacheDataSource.getCourseList(courseListType)
    }

    override fun addCourseToList(courseListType: CourseListType, courseId: Long): Completable =
        courseListCacheDataSource.addCourseToList(courseListType, courseId)

    override fun removeCourseFromList(courseListType: CourseListType, courseId: Long): Completable =
        courseListCacheDataSource.removeCourseFromList(courseListType, courseId)
}