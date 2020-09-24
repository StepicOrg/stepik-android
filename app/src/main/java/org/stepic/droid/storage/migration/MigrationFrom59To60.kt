package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.step.structure.DbStructureStep

object MigrationFrom59To60 : Migration(59, 60) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureStep.TABLE_NAME} ADD COLUMN ${DbStructureStep.Column.SESSION} LONG")
        db.execSQL("ALTER TABLE ${DbStructureStep.TABLE_NAME} ADD COLUMN ${DbStructureStep.Column.INSTRUCTION} LONG")
        db.execSQL("ALTER TABLE ${DbStructureStep.TABLE_NAME} ADD COLUMN ${DbStructureStep.Column.INSTRUCTION_TYPE} TEXT")
    }
}