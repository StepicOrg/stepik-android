package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.purchase_notification.structure.DbStructurePurchaseNotification

object MigrationFrom56To57 : Migration(56, 57) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(DbStructurePurchaseNotification.TABLE_SCHEMA)
    }
}