package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.course_calendar.structure.DbStructureSectionDateEvent

object MigrationFrom37To38 : Migration{
    override fun migrate(db: SQLiteDatabase) {
        DbStructureSectionDateEvent.createTable(db)
    }

}