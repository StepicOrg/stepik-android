package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationFrom77To78 : Migration(77, 78) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `BillingPurchasePayload` (`orderId` TEXT NOT NULL, `courseId` INTEGER NOT NULL, `profileId` INTEGER NOT NULL, `obfuscatedAccountId` TEXT NOT NULL, `obfuscatedProfileId` TEXT NOT NULL, `promoCode` TEXT, PRIMARY KEY(`orderId`))")
    }
}