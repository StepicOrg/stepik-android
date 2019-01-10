package org.stepik.android.cache.personal_deadlines.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureDeadlines {
    const val DEADLINES = "personal_deadlines"

    object Columns {
        const val RECORD_ID = "record_id"
        const val COURSE_ID = "course_id"
        const val SECTION_ID = "section_id"
        const val DEADLINE = "deadline"
    }

    fun createTable(db: SQLiteDatabase) {
        val sql = """
            CREATE TABLE IF NOT EXISTS $DEADLINES (
                ${Columns.RECORD_ID} LONG,
                ${Columns.COURSE_ID} LONG,
                ${Columns.SECTION_ID} LONG,
                ${Columns.DEADLINE} DATETIME,
                PRIMARY KEY(${Columns.SECTION_ID})
            )""".trimIndent() // for section should be only one deadline
        db.execSQL(sql)
    }
}