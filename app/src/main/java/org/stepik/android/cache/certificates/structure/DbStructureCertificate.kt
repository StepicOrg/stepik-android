package org.stepik.android.cache.certificates.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureCertificate {
    const val TABLE_NAME = "certificate"

    object Columns {
        const val ID = "id"
        const val USER = "user"
        const val COURSE = "course"
        const val ISSUE_DATE = "issue_date"
        const val UPDATE_DATE = "update_date"
        const val GRADE = "grade"
        const val TYPE = "type"
        const val URL = "url"
    }

    fun createTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${Columns.ID} LONG PRIMARY KEY,
                ${Columns.USER} LONG,
                ${Columns.COURSE} LONG,
                ${Columns.ISSUE_DATE} LONG,
                ${Columns.UPDATE_DATE} LONG,
                ${Columns.GRADE} TEXT,
                ${Columns.TYPE} INTEGER,
                ${Columns.URL} TEXT
            )
        """.trimIndent())
    }
}