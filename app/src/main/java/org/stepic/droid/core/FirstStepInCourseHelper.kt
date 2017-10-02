package org.stepic.droid.core

import org.stepic.droid.model.Course
import org.stepic.droid.model.Lesson
import org.stepic.droid.model.Section
import org.stepic.droid.model.Unit
import org.stepic.droid.storage.repositories.Repository
import javax.inject.Inject

class FirstStepInCourseHelper @Inject
constructor(
        private val courseRepository: Repository<Course>,
        private val sectionRepository: Repository<Section>,
        private val lessonRepository: Repository<Lesson>,
        private val unitRepository: Repository<Unit>
) {
    public fun getStepIdOfTheFirstStepInCourse(courseId: Long): Long? {
        val sectionId = courseRepository.getObject(courseId)?.sections?.firstOrNull() ?: return null
        val unitId = sectionRepository.getObject(sectionId)?.units?.firstOrNull() ?: return null
        val lessonId = unitRepository.getObject(unitId)?.lesson ?: return null
        return lessonRepository.getObject(lessonId)?.steps?.firstOrNull() ?: return null
    }
}
