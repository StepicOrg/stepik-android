package org.stepic.droid.core

import org.stepik.android.model.structure.Course
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Section
import org.stepik.android.model.structure.Unit
import org.stepic.droid.storage.repositories.Repository
import org.stepic.droid.util.hasUserAccessAndNotEmpty
import javax.inject.Inject

class FirstStepInCourseHelper @Inject
constructor(
        private val courseRepository: Repository<Course>,
        private val sectionRepository: Repository<Section>,
        private val lessonRepository: Repository<Lesson>,
        private val unitRepository: Repository<Unit>
) {

    /**
     * @return stepId of the 1st lesson-unit of the 1st available for user section in the course,
     * or null if it is not exist or no internet connection
     */
    fun getStepIdOfTheFirstAvailableStepInCourse(courseId: Long): Long? {
        val course = courseRepository.getObject(courseId) ?: return null
        val sectionIds = course.sections ?: return null
        val section = sectionRepository
                .getObjects(sectionIds)
                .firstOrNull {
                    it.hasUserAccessAndNotEmpty(course)
                } ?: return null
        val unitId = sectionRepository.getObject(section.id)?.units?.firstOrNull() ?: return null
        val lessonId = unitRepository.getObject(unitId)?.lesson ?: return null
        return lessonRepository.getObject(lessonId)?.steps?.firstOrNull() ?: return null
    }
}