package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.submission.structure.DbStructureSubmission

object MigrationFrom43To44 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        DbStructureSubmission.createTable(db)
    }
}