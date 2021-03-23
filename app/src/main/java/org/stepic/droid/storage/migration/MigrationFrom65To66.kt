package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.step.structure.DbStructureStep

object MigrationFrom65To66 : Migration(65, 66) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureStep.TABLE_NAME} ADD COLUMN ${DbStructureStep.Column.NEEDS_PLAN} TEXT")
        db.execSQL("CREATE TABLE IF NOT EXISTS `CourseRecommendation` (`id` INTEGER NOT NULL, `courses` TEXT NOT NULL, PRIMARY KEY (`id`))")
    }
}