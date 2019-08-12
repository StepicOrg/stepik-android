package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.certificates.structure.DbStructureCertificate

object MigrationFrom45to46: Migration {
    override fun migrate(db: SQLiteDatabase) {
        DbStructureCertificate.createTable(db)
    }
}