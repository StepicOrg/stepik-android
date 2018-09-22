package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.features.stories.storage.structure.DbStructureViewedStoryTemplates

object MigrationFrom34To35 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        DbStructureViewedStoryTemplates.createTable(db)
    }
}