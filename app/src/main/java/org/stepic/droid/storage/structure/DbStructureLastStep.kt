package org.stepic.droid.storage.structure

import androidx.sqlite.db.SupportSQLiteDatabase

object DbStructureLastStep {
    const val TABLE_NAME = "last_step"

    object Columns {
        const val ID = "id"
        const val UNIT_ID = "unit_id"
        const val LESSON_ID = "lesson_id"
        const val STEP_ID = "step_id"
    }

    fun createTable(sql: SupportSQLiteDatabase) {
        sql.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${Columns.ID} TEXT PRIMARY KEY,
                ${Columns.UNIT_ID} LONG,
                ${Columns.LESSON_ID} LONG,
                ${Columns.STEP_ID} LONG
            )
        """.trimIndent())
    }
}