package org.stepik.android.cache.comment_banner.structure

import androidx.sqlite.db.SupportSQLiteDatabase

@Deprecated("Removed structure")
object DbStructureCommentBanner {
    const val TABLE_NAME = "comments_banner"

    object Columns {
        const val COURSE_ID = "course_id"
    }

    fun createTable(db: SupportSQLiteDatabase) {
        val sql = """
            CREATE TABLE IF NOT EXISTS ${DbStructureCommentBanner.TABLE_NAME} (
                ${Columns.COURSE_ID} LONG PRIMARY KEY
            )""".trimIndent()
        db.execSQL(sql)
    }

    fun dropTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            DROP TABLE IF EXISTS ${DbStructureCommentBanner.TABLE_NAME}
        """.trimIndent())
    }
}