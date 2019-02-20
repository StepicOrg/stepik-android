package org.stepik.android.cache.course_reviews.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureCourseReview {
    const val TABLE_NAME = "course_reviews"

    object Columns {
        const val ID = "id"
        const val COURSE = "course"
        const val USER = "user"
        const val SCORE = "score"
        const val TEXT = "text"
        const val CREATE_DATE = "create_date"
        const val UPDATE_DATE = "update_date"
    }

    fun createTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${DbStructureCourseReview.Columns.ID} LONG PRIMARY KEY,

                ${DbStructureCourseReview.Columns.COURSE} LONG,
                ${DbStructureCourseReview.Columns.USER} LONG,
                ${DbStructureCourseReview.Columns.SCORE} INTEGER,
                ${DbStructureCourseReview.Columns.TEXT} TEXT,

                ${DbStructureCourseReview.Columns.CREATE_DATE} LONG,
                ${DbStructureCourseReview.Columns.UPDATE_DATE} LONG
            )
        """.trimIndent())
    }
}