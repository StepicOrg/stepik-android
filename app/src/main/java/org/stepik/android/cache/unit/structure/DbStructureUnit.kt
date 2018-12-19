package org.stepik.android.cache.unit.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureUnit {
    const val TABLE_NAME = "unit"

    object Columns {
        const val ID = "id"
        const val SECTION = "section"
        const val LESSON = "lesson"
        const val ASSIGNMENTS = "assignments"
        const val POSITION = "position"
        const val PROGRESS = "progress"
        const val BEGIN_DATE = "begin_date"
        const val END_DATE = "end_date"
        const val SOFT_DEADLINE = "soft_deadline"
        const val HARD_DEADLINE = "hard_deadline"
        const val GRADING_POLICY = "grading_policy"
        const val BEGIN_DATE_SOURCE = "begin_date_source"
        const val END_DATE_SOURCE = "end_date_source"
        const val SOFT_DEADLINE_SOURCE = "soft_deadline_source"
        const val HARD_DEADLINE_SOURCE = "hard_deadline_source"
        const val GRADING_POLICY_SOURCE = "grading_policy_source"
        const val IS_ACTIVE = "is_active"
        const val CREATE_DATE = "create_date"
        const val UPDATE_DATE = "update_date"
    }

    fun createTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${DbStructureUnit.Columns.ID} LONG,
                ${DbStructureUnit.Columns.SECTION} LONG,
                ${DbStructureUnit.Columns.LESSON} LONG,
                ${DbStructureUnit.Columns.ASSIGNMENTS} TEXT,
                ${DbStructureUnit.Columns.POSITION} INTEGER,
                ${DbStructureUnit.Columns.PROGRESS} TEXT,
                ${DbStructureUnit.Columns.BEGIN_DATE} LONG,
                ${DbStructureUnit.Columns.END_DATE} LONG,
                ${DbStructureUnit.Columns.SOFT_DEADLINE} LONG,
                ${DbStructureUnit.Columns.HARD_DEADLINE} LONG,
                ${DbStructureUnit.Columns.GRADING_POLICY} TEXT,
                ${DbStructureUnit.Columns.BEGIN_DATE_SOURCE} TEXT,
                ${DbStructureUnit.Columns.END_DATE_SOURCE} TEXT,
                ${DbStructureUnit.Columns.SOFT_DEADLINE_SOURCE} TEXT,
                ${DbStructureUnit.Columns.HARD_DEADLINE_SOURCE} TEXT,
                ${DbStructureUnit.Columns.GRADING_POLICY_SOURCE} TEXT,
                ${DbStructureUnit.Columns.IS_ACTIVE} INTEGER,
                ${DbStructureUnit.Columns.CREATE_DATE} LONG,
                ${DbStructureUnit.Columns.UPDATE_DATE} LONG
            )
        """.trimIndent())
    }
}