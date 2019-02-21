package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.course_reviews.structure.DbStructureCourseReview

object MigrationFrom37To38 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        DbStructureCourseReview.createTable(db)
    }
}