package org.stepik.android.cache.course.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.CourseListType
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.util.PagedList
import org.stepik.android.data.course.source.CourseListCacheDataSource
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseListCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : CourseListCacheDataSource {
    override fun getCourseList(courseListType: CourseListType): Single<PagedList<Course>> =
        Single.fromCallable {
            PagedList(databaseFacade.getAllCourses(courseListType))
        }

    override fun addCourseToList(courseListType: CourseListType, courseId: Long): Completable =
        Completable.fromAction {
            databaseFacade.addCourseToList(courseListType, courseId)
        }

    override fun removeCourseFromList(courseListType: CourseListType, courseId: Long): Completable =
        Completable.fromAction {
            databaseFacade.deleteCourseFromList(courseListType, courseId)
        }
}