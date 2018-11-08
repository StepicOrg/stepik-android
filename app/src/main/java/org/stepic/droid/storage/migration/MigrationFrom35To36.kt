package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureLesson

object MigrationFrom35To36 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        val sql = """
            ALTER TABLE ${DbStructureLesson.LESSONS}
            ADD COLUMN ${DbStructureLesson.Column.VOTE_DELTA} LONG
            DEFAULT 0
        """.trimIndent()
        db.execSQL(sql)
    }
}