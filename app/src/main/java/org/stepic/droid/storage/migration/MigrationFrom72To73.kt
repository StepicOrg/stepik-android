package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.lesson.structure.DbStructureLesson

object MigrationFrom72To73 : Migration(72, 73) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureLesson.TABLE_NAME} ADD COLUMN ${DbStructureLesson.Columns.UNITS} TEXT")
    }
}