package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.step.structure.DbStructureStep

object MigrationFrom62To63 : Migration(62, 63) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureStep.TABLE_NAME} ADD COLUMN ${DbStructureStep.Column.IS_ENABLED} INTEGER")
        db.execSQL("CREATE TABLE IF NOT EXISTS `CatalogBlockItem` (`id` INTEGER NOT NULL, `position` INTEGER NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `language` TEXT NOT NULL, `appearance` TEXT NOT NULL, `isTitleVisible` INTEGER NOT NULL, `content` TEXT NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `StoryReactionEntity` (`id` INTEGER NOT NULL, `reaction` TEXT NOT NULL, PRIMARY KEY(`id`))")
    }
}