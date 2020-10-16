package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.features.stories.storage.structure.DbStructureViewedStoryTemplates

object MigrationFrom34To35 : Migration(34, 35) {
    override fun migrate(db: SupportSQLiteDatabase) {
        DbStructureViewedStoryTemplates.createTable(db)
    }
}