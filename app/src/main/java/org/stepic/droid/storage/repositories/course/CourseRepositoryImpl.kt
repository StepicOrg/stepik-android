package org.stepic.droid.storage.repositories.course

import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.web.Api
import org.stepik.android.data.course.source.CourseRemoteDataSource
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseRepositoryImpl
@Inject
constructor(
        private val databaseFacade: DatabaseFacade,
        private val courseRemoteDataSource: CourseRemoteDataSource,
        private val api: Api
) : Repository<Course> {

    override fun getObject(key: Long): Course? =
        databaseFacade
            .getCourseById(key)
            ?: try {
                courseRemoteDataSource.getCourses(key).execute()?.body()?.courses?.firstOrNull()
            } catch (exception: Exception) {
                null
            }

    override fun getObjects(keys: LongArray): Iterable<Course> {
        TODO()
    }

}
