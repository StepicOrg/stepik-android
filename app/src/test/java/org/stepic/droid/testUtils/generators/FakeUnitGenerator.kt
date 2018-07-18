package org.stepic.droid.testUtils.generators

import org.stepik.android.model.structure.Unit

object FakeUnitGenerator {

    @JvmOverloads
    fun generate(
            unitId : Long = 0L,
            sectionId: Long = 0L,
            position : Int = 1,
            lessonId: Long = 0
    ): Unit = Unit(id = unitId, section = sectionId, position = position, lesson = lessonId)
}
