package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.purchase_notification.structure.DbStructurePurchaseNotification

object MigrationFrom56To57 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        db.execSQL(DbStructurePurchaseNotification.TABLE_SCHEMA)
    }
}