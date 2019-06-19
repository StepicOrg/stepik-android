package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.course.source.structure.DbStructureCourseReviewSummary

object MigrationFrom41To42 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        DbStructureCourseReviewSummary.createTable(db)
    }
}