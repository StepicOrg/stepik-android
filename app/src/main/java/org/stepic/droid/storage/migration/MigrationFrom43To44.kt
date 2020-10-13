package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.submission.structure.DbStructureSubmission

object MigrationFrom43To44 : Migration(43, 44) {
    override fun migrate(db: SupportSQLiteDatabase) {
        DbStructureSubmission.createTable(db)
    }
}