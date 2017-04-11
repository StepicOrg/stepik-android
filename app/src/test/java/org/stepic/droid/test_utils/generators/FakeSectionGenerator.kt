package org.stepic.droid.test_utils.generators

import org.stepic.droid.model.Section

object FakeSectionGenerator {
    @JvmOverloads
    fun generate(sectionId: Long = 0,
                 unitIds: LongArray? = null,
                 position: Int = 1): Section {
        val section = Section()
        section.id = sectionId
        section.position = position
        section.units = unitIds
        return section
    }
}
