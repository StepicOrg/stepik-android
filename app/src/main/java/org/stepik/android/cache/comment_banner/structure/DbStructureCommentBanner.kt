package org.stepik.android.cache.comment_banner.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureCommentBanner {
    const val TABLE_NAME = "comments_banner"

    object Columns {
        const val COURSE_ID = "course_id"
    }

    fun createTable(db: SQLiteDatabase) {
        val sql = """
            CREATE TABLE IF NOT EXISTS ${DbStructureCommentBanner.TABLE_NAME} (
                ${Columns.COURSE_ID} LONG PRIMARY KEY
            )""".trimIndent()
        db.execSQL(sql)
    }

    fun dropTable(db: SQLiteDatabase) {
        db.execSQL("""
            DROP TABLE IF EXISTS ${DbStructureCommentBanner.TABLE_NAME}
        """.trimIndent())
    }
}