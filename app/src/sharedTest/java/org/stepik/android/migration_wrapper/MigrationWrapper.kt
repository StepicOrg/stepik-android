package org.stepik.android.migration_wrapper

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

abstract class MigrationWrapper(val migration: Migration) {
    open fun beforeTest(db: SupportSQLiteDatabase) {}
    open fun afterTest(db: SupportSQLiteDatabase) {}
}