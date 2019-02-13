package org.stepik.android.cache.course_calendar.structure

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.lesson.structure.DbStructureLesson

object DbStructureSectionDateEvent {
    const val TABLE_NAME = "course_calendar"

    object Columns {
        const val EVENT_ID = "event_id"
        const val SECTION_ID = "section_id"
    }

    fun createTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ${DbStructureLesson.TABLE_NAME} (
                ${DbStructureSectionDateEvent.Columns.EVENT_ID} LONG PRIMARY KEY,
                ${DbStructureSectionDateEvent.Columns.SECTION_ID} LONG
            )
        """.trimIndent())
    }
}