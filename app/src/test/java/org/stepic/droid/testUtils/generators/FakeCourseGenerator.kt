package org.stepic.droid.testUtils.generators

import org.stepik.android.model.Course

object FakeCourseGenerator {
    @JvmOverloads
    fun generate(
            courseId: Long = 0,
            sectionIds: List<Long>? = null,
            instructors: List<Long>? = null
    ): Course =
            Course(id = courseId, sections = sectionIds, instructors = instructors)
}