package org.stepik.android.cache.course.source

import io.reactivex.Completable
import io.reactivex.Single
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepik.android.data.course.source.CourseCacheDataSource
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseCacheDataSourceImpl
@Inject
constructor(
    private val databaseFacade: DatabaseFacade
) : CourseCacheDataSource {
    override fun getCourses(ids: List<Long>): Single<List<Course>> =
        Single.fromCallable {
            ids.mapNotNull(databaseFacade::getCourseById)
        }

    override fun saveCourses(courses: List<Course>): Completable =
        Completable.fromAction {
            databaseFacade.addCourses(courses)
        }

    override fun removeCourse(courseId: Long): Completable =
        Completable.fromAction {
            databaseFacade.deleteCourse(courseId)
        }

    override fun removeCachedCourses(): Completable =
        Completable.fromAction {
            databaseFacade.deleteCourses()
        }
}