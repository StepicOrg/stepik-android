package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationFrom66To67 : Migration(66, 67) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `Rubric` (`id` INTEGER NOT NULL, `instruction` INTEGER NOT NULL, `text` TEXT NOT NULL, `cost` INTEGER NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY (`id`))")
    }
}