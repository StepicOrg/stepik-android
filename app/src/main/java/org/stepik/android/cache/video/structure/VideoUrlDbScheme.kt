package org.stepik.android.cache.video.structure

import android.database.sqlite.SQLiteDatabase

object VideoUrlDbScheme {
    const val TABLE_NAME = "video_urls"

    object Columns {
        const val VIDEO_ID = "video_id"
        const val URL = "url"
        const val QUALITY = "quality"
    }

    fun createTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ${VideoUrlDbScheme.TABLE_NAME} (
                ${VideoUrlDbScheme.Columns.VIDEO_ID} LONG,
                ${VideoUrlDbScheme.Columns.URL} TEXT,
                ${VideoUrlDbScheme.Columns.QUALITY} TEXT,
                PRIMARY KEY (${VideoUrlDbScheme.Columns.VIDEO_ID}, ${VideoUrlDbScheme.Columns.QUALITY})
            )
        """.trimIndent())
    }
}