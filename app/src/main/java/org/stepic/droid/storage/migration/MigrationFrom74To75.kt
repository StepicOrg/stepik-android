package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationFrom74To75 : Migration(74, 75) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `WishlistEntry` (`id` INTEGER NOT NULL, `course` INTEGER NOT NULL, `user` INTEGER NOT NULL, `createDate` INTEGER NOT NULL, `platform` TEXT NOT NULL, PRIMARY KEY(`id`))")
    }
}