package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// TODO APPS-3254 Can be combined with some other migration from other active PRs in current release
object MigrationFrom65To66 : Migration(65, 66) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `CourseRecommendation` (`id` INTEGER NOT NULL, `courses` TEXT NOT NULL, PRIMARY KEY (`id`))")
    }
}