package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.attempt.structure.DbStructureAttempt

object MigrationFrom47To48 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        db.execSQL(DbStructureAttempt.TABLE_SCHEMA)
    }
}