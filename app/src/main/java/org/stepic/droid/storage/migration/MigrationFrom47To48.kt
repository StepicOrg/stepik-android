package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.attempt.structure.DbStructureAttempt

object MigrationFrom47To48 : Migration(47, 48) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(DbStructureAttempt.TABLE_SCHEMA)
    }
}