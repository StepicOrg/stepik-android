package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.course_reviews.structure.DbStructureCourseReview

object MigrationFrom37To38 : Migration(37, 38) {
    override fun migrate(db: SupportSQLiteDatabase) {
        DbStructureCourseReview.createTable(db)
    }
}