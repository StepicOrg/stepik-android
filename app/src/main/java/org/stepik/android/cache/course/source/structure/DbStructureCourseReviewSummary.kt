package org.stepik.android.cache.course.source.structure

import androidx.sqlite.db.SupportSQLiteDatabase

object DbStructureCourseReviewSummary {
    const val TABLE_NAME = "course_summary"

    object Columns {
        const val SUMMARY_ID = "summary_id"
        const val COURSE_ID = "course_id"
        const val AVERAGE = "average"

        const val COUNT = "count"
        const val DISTRIBUTION = "distribution"
    }

    fun createTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${Columns.SUMMARY_ID} LONG PRIMARY KEY,
                ${Columns.COURSE_ID} LONG,
                ${Columns.AVERAGE} REAL
            )
        """.trimIndent())
    }
}