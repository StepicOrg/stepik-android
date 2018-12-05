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
    override fun getCourses(vararg ids: Long): Single<List<Course>> =
        Single.create { emitter ->
            ids.map(databaseFacade::getCourseById)
                .filterNotNull()
                .let(emitter::onSuccess)
        }

    override fun saveCourse(course: Course): Completable =
        Completable.fromAction {
            databaseFacade.addCourse(course)
        }
}