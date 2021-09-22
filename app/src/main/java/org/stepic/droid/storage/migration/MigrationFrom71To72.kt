package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureSearchQuery

object MigrationFrom71To72 : Migration(71, 72) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureSearchQuery.SEARCH_QUERY} ADD COLUMN ${DbStructureSearchQuery.Column.QUERY_COURSE_ID} INTEGER DEFAULT -1")
    }
}