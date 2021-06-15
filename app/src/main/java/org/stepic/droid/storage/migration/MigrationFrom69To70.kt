package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationFrom69To70 : Migration(69, 70) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `WishlistEntity` (`recordId` INTEGER NOT NULL, `courses` TEXT NOT NULL, PRIMARY KEY (`recordId`))")
    }
}