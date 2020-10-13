package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.course.source.structure.DbStructureCourseReviewSummary

object MigrationFrom51To52 : Migration(51, 52) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureCourseReviewSummary.TABLE_NAME} ADD COLUMN ${DbStructureCourseReviewSummary.Columns.COUNT} LONG")
        db.execSQL("ALTER TABLE ${DbStructureCourseReviewSummary.TABLE_NAME} ADD COLUMN ${DbStructureCourseReviewSummary.Columns.DISTRIBUTION} TEXT")
    }
}