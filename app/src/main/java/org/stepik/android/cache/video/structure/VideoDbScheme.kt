package org.stepik.android.cache.video.structure

import android.database.sqlite.SQLiteDatabase

object VideoDbScheme {
    const val TABLE_NAME = "video"

    object Columns {
        const val ID = "id"
        const val THUMBNAIL = "thumbnail"
        const val DURATION = "duration"
    }

    fun createTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ${VideoDbScheme.TABLE_NAME} (
                ${VideoDbScheme.Columns.ID} LONG PRIMARY KEY,
                ${VideoDbScheme.Columns.THUMBNAIL} TEXT,
                ${VideoDbScheme.Columns.DURATION} LONG
            )
        """.trimIndent())
    }
}