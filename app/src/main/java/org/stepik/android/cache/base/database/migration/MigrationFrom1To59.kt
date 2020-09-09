package org.stepik.android.cache.base.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.storage.DatabaseHelper

object MigrationFrom1To59 : Migration(1, 59) {
    override fun migrate(database: SupportSQLiteDatabase) {
        DatabaseHelper().onUpgrade(database, 1, 59)
    }
}