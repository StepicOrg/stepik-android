package org.stepik.android.cache.personal_deadlines.structure

import androidx.sqlite.db.SupportSQLiteDatabase

object DbStructureDeadlinesBanner {
    const val DEADLINES_BANNER = "deadlines_banner"

    object Columns {
        const val COURSE_ID = "course_id"
    }

    fun createTable(db: SupportSQLiteDatabase) {
        val sql = """
            CREATE TABLE IF NOT EXISTS $DEADLINES_BANNER (
                ${Columns.COURSE_ID} LONG PRIMARY KEY
            )""".trimIndent()
        db.execSQL(sql)
    }
}