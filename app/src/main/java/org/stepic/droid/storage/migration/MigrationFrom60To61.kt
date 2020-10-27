package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationFrom60To61 : Migration(60, 61) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `VisitedCourse` (`id` INTEGER NOT NULL, `course` INTEGER NOT NULL, PRIMARY KEY(`course`))")
    }
}