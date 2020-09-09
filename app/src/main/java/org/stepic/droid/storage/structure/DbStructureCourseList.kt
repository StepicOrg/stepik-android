package org.stepic.droid.storage.structure

import androidx.sqlite.db.SupportSQLiteDatabase

@Deprecated("Removed structure")
object DbStructureCourseList {
    private const val TABLE_NAME = "course_list"

    fun dropTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            DROP TABLE IF EXISTS $TABLE_NAME
        """.trimIndent())
    }
}