package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationFrom63To64 : Migration(63, 64) {
    override fun migrate(database: SupportSQLiteDatabase) {}
}