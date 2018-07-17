package org.stepic.droid.testUtils.generators

import org.stepik.android.model.structure.Unit

object FakeUnitGenerator {

    @JvmOverloads
    fun generate(
            unitId : Long = 0L,
            sectionId: Long = 0L,
            position : Int = 1): Unit {
        val unit = Unit()
        unit.id = unitId
        unit.section = sectionId
        unit.position = position
        return unit
    }
}
