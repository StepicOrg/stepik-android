package org.stepic.droid.storage.structure

import androidx.sqlite.db.SupportSQLiteDatabase

@Deprecated("Removed structure")
object DbStructureCourseList {
    const val TABLE_NAME = "course_list"

    object Columns {
        const val TYPE = "type"
        const val COURSE_ID = "course_id"
    }

    fun createTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${Columns.TYPE} TEXT,
                ${Columns.COURSE_ID} LONG,
                PRIMARY KEY (${Columns.TYPE}, ${Columns.COURSE_ID})
            )
        """.trimIndent())
    }

    fun dropTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            DROP TABLE IF EXISTS $TABLE_NAME
        """.trimIndent())
    }
}