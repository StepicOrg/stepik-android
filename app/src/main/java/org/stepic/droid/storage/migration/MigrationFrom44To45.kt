package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.block.structure.DbStructureBlock

object MigrationFrom44To45 : Migration(44, 45) {
    override fun migrate(db: SupportSQLiteDatabase) {
        createBlocksTable(db)
        migrateBlocks(db)
    }

    private fun createBlocksTable(db: SupportSQLiteDatabase) {
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

    private fun migrateBlocks(db: SupportSQLiteDatabase) {
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