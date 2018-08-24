package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase

interface Migration {
    fun migrate(db: SQLiteDatabase)
}