package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureCourse

object MigrationFrom70To71 : Migration(70, 71) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureCourse.TABLE_NAME} ADD COLUMN ${DbStructureCourse.Columns.WITH_CERTIFICATE} INTEGER")
    }
}