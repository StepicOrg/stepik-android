package org.stepik.android.cache.course_calendar.structure

import androidx.sqlite.db.SupportSQLiteDatabase

object DbStructureSectionDateEvent {
    const val TABLE_NAME = "course_calendar"

    object Columns {
        const val EVENT_ID = "event_id"
        const val SECTION_ID = "section_id"
    }

    fun createTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${Columns.EVENT_ID} LONG PRIMARY KEY,
                ${Columns.SECTION_ID} LONG
            )
        """.trimIndent())
    }
}