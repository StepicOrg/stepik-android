package org.stepik.android.cache.certificates.structure

import androidx.sqlite.db.SupportSQLiteDatabase

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

        const val USER_RANK = "user_rank"
        const val USER_RANK_MAX = "user_rank_max"
        const val LEADERBOARD_SIZE = "leaderboard_size"
        const val PREVIEW_URL = "preview_url"
        const val SAVED_FULLNAME = "saved_fullname"
        const val EDITS_COUNT = "edits_count"
        const val ALLOWED_EDITS_COUNT = "allowed_edits_count"
    }

    fun createTable(db: SupportSQLiteDatabase) {
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