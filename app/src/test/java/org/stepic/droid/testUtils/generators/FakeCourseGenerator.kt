package org.stepic.droid.testUtils.generators

import org.stepik.android.model.structure.Course

object FakeCourseGenerator {
    @JvmOverloads
    fun generate(courseId: Long = 0,
                 sectionIds: LongArray? = null): Course {
        val course = Course()
        course.setId(courseId)
        course.sections = sectionIds
        return course
    }
}