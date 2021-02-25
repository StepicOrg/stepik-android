package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationFrom64To65 : Migration(64, 65) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `CodePreference` (`languagesKey` TEXT NOT NULL, `preferredLanguage` TEXT NOT NULL, PRIMARY KEY('languagesKey'))")
    }
}