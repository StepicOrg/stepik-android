package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.course.source.structure.DbStructureCourseReviewSummary

object MigrationFrom41To42 : Migration(41, 42) {
    override fun migrate(db: SupportSQLiteDatabase) {
        DbStructureCourseReviewSummary.createTable(db)
    }
}