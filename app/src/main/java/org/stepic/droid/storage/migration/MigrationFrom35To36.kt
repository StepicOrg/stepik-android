package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureLesson

object MigrationFrom35To36 : Migration(35, 36) {
    override fun migrate(db: SupportSQLiteDatabase) {
        val sql = """
            ALTER TABLE ${DbStructureLesson.LESSONS}
            ADD COLUMN ${DbStructureLesson.Column.VOTE_DELTA} LONG
            DEFAULT 0
        """.trimIndent()
        db.execSQL(sql)
    }
}