package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureCourse

object MigrationFrom79To80 : Migration(79, 80) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureCourse.TABLE_NAME} ADD COLUMN ${DbStructureCourse.Columns.ACQUIRED_SKILLS} TEXT")
    }
}