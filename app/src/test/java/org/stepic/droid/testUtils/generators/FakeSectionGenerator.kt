package org.stepic.droid.testUtils.generators

import org.stepik.android.model.structure.Section

object FakeSectionGenerator {
    @JvmOverloads
    fun generate(sectionId: Long = 0,
                 unitIds: LongArray = longArrayOf(),
                 position: Int = 1,
                 courseId: Long = 123,
                 isActive : Boolean = true): Section {
        val section = Section()
        section.id = sectionId
        section.position = position
        section.units = unitIds
        section.course = courseId
        section.isActive = isActive
        return section
    }
}
