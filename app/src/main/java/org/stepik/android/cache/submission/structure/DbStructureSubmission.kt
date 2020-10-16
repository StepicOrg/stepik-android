package org.stepik.android.cache.submission.structure

import androidx.sqlite.db.SupportSQLiteDatabase

object DbStructureSubmission {
    const val TABLE_NAME = "submission"

    object Columns {
        const val ID = "id"
        const val STATUS = "status"
        const val SCORE = "score"
        const val HINT = "hint"
        const val TIME = "time"
        const val REPLY = "reply"
        const val ATTEMPT_ID = "attempt_id"
        const val SESSION = "session"
        const val ETA = "eta"
        const val FEEDBACK = "feedback"
    }

    fun createTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${Columns.ID} LONG,
                ${Columns.STATUS} INTEGER,
                ${Columns.SCORE} TEXT,
                ${Columns.HINT} TEXT,
                ${Columns.TIME} TEXT,
                ${Columns.REPLY} TEXT,
                ${Columns.ATTEMPT_ID} LONG PRIMARY KEY,
                ${Columns.SESSION} TEXT,
                ${Columns.ETA} TEXT,
                ${Columns.FEEDBACK} TEXT
            )
        """)
    }
}