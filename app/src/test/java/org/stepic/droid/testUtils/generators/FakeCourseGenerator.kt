package org.stepic.droid.testUtils.generators

import org.stepik.android.model.Course

object FakeCourseGenerator {
    @JvmOverloads
    fun generate(
            courseId: Long = 0,
            sectionIds: LongArray? = null,
            instructors: LongArray? = null
    ): Course =
            Course(id = courseId, sections = sectionIds, instructors = instructors)
}