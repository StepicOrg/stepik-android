package org.stepic.droid.storage.repositories.course

import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.web.Api
import org.stepik.android.model.Course
import javax.inject.Inject

class CourseRepositoryImpl
@Inject
constructor(
        private val databaseFacade: DatabaseFacade,
        private val api: Api
) : Repository<Course> {

    override fun getObject(key: Long): Course? =
        databaseFacade
            .getCourseById(key)
            ?: try {
                api.getCourse(key).execute()?.body()?.courses?.firstOrNull()
            } catch (exception: Exception) {
                null
            }

    override fun getObjects(keys: LongArray): Iterable<Course> {
        TODO()
    }

}
