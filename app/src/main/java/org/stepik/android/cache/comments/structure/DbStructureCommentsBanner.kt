package org.stepik.android.cache.comments.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureCommentsBanner {
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