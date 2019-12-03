package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.step.structure.DbStructureStep

object MigrationFrom48To49 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE ${DbStructureStep.TABLE_NAME} ADD COLUMN ${DbStructureStep.Column.CORRECT_RATIO} REAL
        """.trimIndent())
    }
}