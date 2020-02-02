package org.stepik.android.cache.course.source.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureCourseReviewSummary {
    const val TABLE_NAME = "course_summary"

    object Columns {
        const val SUMMARY_ID = "summary_id"
        const val COURSE_ID = "course_id"
        const val AVERAGE = "average"

        const val COUNT = "count"
        const val DISTRIBUTION = "distribution"
    }

    fun createTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ${DbStructureCourseReviewSummary.TABLE_NAME} (
                ${DbStructureCourseReviewSummary.Columns.SUMMARY_ID} LONG PRIMARY KEY,
                ${DbStructureCourseReviewSummary.Columns.COURSE_ID} LONG,
                ${DbStructureCourseReviewSummary.Columns.AVERAGE} REAL
            )
        """.trimIndent())
    }
}