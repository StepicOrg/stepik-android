package org.stepic.droid.storage.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureLastStep {
    const val TABLE_NAME = "last_step"

    object Columns {
        const val ID = "id"
        const val UNIT_ID = "unit_id"
        const val STEP_ID = "step_id"
    }

    fun createTable(sql: SQLiteDatabase) {
        sql.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${Columns.ID} TEXT PRIMARY KEY,
                ${Columns.UNIT_ID} LONG,
                ${Columns.STEP_ID} LONG
            )
        """.trimIndent())
    }
}