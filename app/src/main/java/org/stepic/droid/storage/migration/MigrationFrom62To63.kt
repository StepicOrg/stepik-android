package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.step.structure.DbStructureStep

object MigrationFrom62To63 : Migration(62, 63) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureStep.TABLE_NAME} ADD COLUMN ${DbStructureStep.Column.IS_ENABLED} INTEGER")
    }
}