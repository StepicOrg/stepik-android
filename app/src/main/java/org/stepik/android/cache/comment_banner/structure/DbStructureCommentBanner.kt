package org.stepik.android.cache.comment_banner.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureCommentBanner {
    const val COMMENTS_BANNER = "comments_banner"

    object Columns {
        const val COURSE_ID = "course_id"
    }

    fun createTable(db: SQLiteDatabase) {
        val sql = """
            CREATE TABLE IF NOT EXISTS $COMMENTS_BANNER (
                ${Columns.COURSE_ID} LONG PRIMARY KEY
            )""".trimIndent()
        db.execSQL(sql)
    }
}