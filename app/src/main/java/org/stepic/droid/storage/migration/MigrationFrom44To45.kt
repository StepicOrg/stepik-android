package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.block.structure.DbStructureBlock

object MigrationFrom44To45 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        createBlocksTable(db)
        migrateBlocks(db)
    }

    private fun createBlocksTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ${DbStructureBlock.TABLE_NAME} (
                ${DbStructureBlock.Column.STEP_ID} LONG PRIMARY KEY,
                ${DbStructureBlock.Column.NAME} TEXT,
                ${DbStructureBlock.Column.TEXT} TEXT,
                ${DbStructureBlock.Column.EXTERNAL_THUMBNAIL} TEXT,
                ${DbStructureBlock.Column.EXTERNAL_VIDEO_ID} LONG,
                ${DbStructureBlock.Column.EXTERNAL_VIDEO_DURATION} LONG,
                ${DbStructureBlock.Column.CODE_OPTIONS} TEXT
            )
        """.trimIndent())
    }

    private fun migrateBlocks(db: SQLiteDatabase) {
        db.execSQL("""
            REPLACE INTO ${DbStructureBlock.TABLE_NAME}
            SELECT
            ${org.stepic.droid.storage.structure.DbStructureBlock.Column.STEP_ID},
            ${org.stepic.droid.storage.structure.DbStructureBlock.Column.NAME},
            ${org.stepic.droid.storage.structure.DbStructureBlock.Column.TEXT},
            ${org.stepic.droid.storage.structure.DbStructureBlock.Column.EXTERNAL_THUMBNAIL},
            ${org.stepic.droid.storage.structure.DbStructureBlock.Column.EXTERNAL_VIDEO_ID},
            ${org.stepic.droid.storage.structure.DbStructureBlock.Column.EXTERNAL_VIDEO_DURATION},
            ${org.stepic.droid.storage.structure.DbStructureBlock.Column.CODE_OPTIONS}
        FROM ${org.stepic.droid.storage.structure.DbStructureBlock.BLOCKS}
        """.trimIndent())
    }
}