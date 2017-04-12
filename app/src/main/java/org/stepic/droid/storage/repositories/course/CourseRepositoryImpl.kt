package org.stepic.droid.storage.repositories.course

import org.stepic.droid.model.Course
import org.stepic.droid.storage.operations.DatabaseFacade
import org.stepic.droid.storage.operations.Table
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.web.Api
import javax.inject.Inject

class CourseRepositoryImpl
@Inject constructor(
        private val databaseFacade: DatabaseFacade,
        private val api: Api)
    : Repository<Course, Long> {

    override fun getObject(key: Long): Course? {
        var course = databaseFacade.getCourseById(key, Table.enrolled)
        if (course == null) {
            course = databaseFacade.getCourseById(key, Table.featured)
        }
        if (course == null) {
            course =
                    try {
                        api.getCourse(key).execute()?.body()?.courses?.firstOrNull()
                    } catch (exception: Exception) {
                        null
                    }
        }
        return course
    }

}
