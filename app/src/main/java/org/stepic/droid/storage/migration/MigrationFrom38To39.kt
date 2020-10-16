package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.course_calendar.structure.DbStructureSectionDateEvent

object MigrationFrom38To39 : Migration(38, 39) {
    override fun migrate(db: SupportSQLiteDatabase) {
        DbStructureSectionDateEvent.createTable(db)
    }

}