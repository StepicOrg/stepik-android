package org.stepic.droid.test_utils.generators

import org.stepic.droid.model.Course

object FakeCourseGenerator {
    fun generate(courseId: Long): Course {
        val course = Course()
        course.setId(courseId)
        return course
    }
}