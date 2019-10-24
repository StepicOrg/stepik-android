package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.lesson.structure.DbStructureLesson
import org.stepik.android.cache.step.structure.DbStructureStep

object MigrationFrom46To47 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE ${DbStructureLesson.TABLE_NAME} ADD COLUMN ${DbStructureLesson.Columns.ACTIONS} TEXT
        """.trimIndent())

        db.execSQL("""
            ALTER TABLE ${DbStructureStep.TABLE_NAME} ADD COLUMN ${DbStructureStep.Column.DISCUSSION_THREADS} TEXT
        """.trimIndent())
    }
}