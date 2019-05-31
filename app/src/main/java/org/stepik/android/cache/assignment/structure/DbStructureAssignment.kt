package org.stepik.android.cache.assignment.structure

object DbStructureAssignment {
    const val TABLE_NAME = "assignment"

    object Columns {
        const val ID = "id"
        const val STEP = "step"
        const val UNIT = "unit"
        const val PROGRESS = "progress"

        const val CREATE_DATE = "create_date"
        const val UPDATE_DATE = "update_date"
    }
}