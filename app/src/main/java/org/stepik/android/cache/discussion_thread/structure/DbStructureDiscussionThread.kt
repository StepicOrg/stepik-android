package org.stepik.android.cache.discussion_thread.structure

import androidx.sqlite.db.SupportSQLiteDatabase

object DbStructureDiscussionThread {
    const val TABLE_NAME = "discussion_thread"

    object Columns {
        const val ID = "id"
        const val THREAD = "thread"
        const val DISCUSSIONS_COUNT = "discussions_count"
        const val DISCUSSION_PROXY = "discussion_proxy"
    }

    fun createTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${Columns.ID} LONG PRIMARY KEY,
                ${Columns.THREAD} TEXT,
                ${Columns.DISCUSSIONS_COUNT} INTEGER,
                ${Columns.DISCUSSION_PROXY} TEXT
            )
        """.trimIndent())
    }
}