package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.course.source.structure.DbStructureCourseReviewSummary

object MigrationFrom51To52 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureCourseReviewSummary.TABLE_NAME} ADD COLUMN ${DbStructureCourseReviewSummary.Columns.COUNT} LONG")
        db.execSQL("ALTER TABLE ${DbStructureCourseReviewSummary.TABLE_NAME} ADD COLUMN ${DbStructureCourseReviewSummary.Columns.DISTRIBUTION} TEXT")
    }
}