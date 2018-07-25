package org.stepic.droid.testUtils.generators

import org.stepik.android.model.Section

object FakeSectionGenerator {
    @JvmOverloads
    fun generate(sectionId: Long = 0,
                 unitIds: List<Long> = emptyList(),
                 position: Int = 1,
                 courseId: Long = 123,
                 isActive : Boolean = true): Section = Section(
            id = sectionId,
            position = position,
            units = unitIds,
            course = courseId,
            isActive = isActive
    )
}
