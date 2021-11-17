package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationFrom73To74 : Migration(73, 74) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `MobileTier` (`id` TEXT NOT NULL, `course` INTEGER NOT NULL, `priceTier` TEXT, `promoTier` TEXT, PRIMARY KEY(`course`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `LightSku` (`id` TEXT NOT NULL, `price` TEXT NOT NULL, PRIMARY KEY(`id`))")
    }
}