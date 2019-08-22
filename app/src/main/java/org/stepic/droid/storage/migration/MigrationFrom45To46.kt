package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.block.structure.DbStructureBlock

object MigrationFrom45To46 : Migration {
    private const val CODE_SUBMISSION = "code_submission"

    override fun migrate(db: SQLiteDatabase) {
        db.execSQL("""
            DROP TABLE IF EXISTS $CODE_SUBMISSION
        """.trimIndent())
    }
}