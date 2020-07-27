package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.analytic.structure.DbStructureAnalytic

object MigrationFrom55To56 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        db.execSQL(DbStructureAnalytic.TABLE_SCHEMA)
    }
}