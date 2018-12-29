package org.stepik.android.cache.course_list

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.model.CourseListType
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.course_list.source.CourseListCacheDataSource
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseListCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : CourseListCacheDataSource {
    override fun getCourseList(courseListType: CourseListType): Single<List<Course>> =
        Single.fromCallable {
            databaseFacade.getAllCourses(courseListType)
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