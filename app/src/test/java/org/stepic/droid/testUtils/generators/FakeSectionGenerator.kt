package org.stepic.droid.testUtils.generators

import org.stepic.droid.model.Section

object FakeSectionGenerator {
    @JvmOverloads
    fun generate(sectionId: Long = 0,
                 unitIds: LongArray? = null,
                 position: Int = 1,
                 courseId: Long = 123,
                 isActive : Boolean = true): Section {
        val section = Section()
        section.id = sectionId
        section.position = position
        section.units = unitIds
        section.course = courseId
        section.is_active = isActive
        return section
    }
}
