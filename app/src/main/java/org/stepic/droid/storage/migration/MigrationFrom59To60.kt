package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.step.structure.DbStructureStep

object MigrationFrom59To60 : Migration(59, 60) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureStep.TABLE_NAME} ADD COLUMN ${DbStructureStep.Column.SESSION} LONG")
        db.execSQL("ALTER TABLE ${DbStructureStep.TABLE_NAME} ADD COLUMN ${DbStructureStep.Column.INSTRUCTION} LONG")
        db.execSQL("ALTER TABLE ${DbStructureStep.TABLE_NAME} ADD COLUMN ${DbStructureStep.Column.INSTRUCTION_TYPE} TEXT")

        db.execSQL("CREATE TABLE IF NOT EXISTS `ReviewInstruction` (`id` INTEGER NOT NULL, `step` INTEGER NOT NULL, `minReviews` INTEGER NOT NULL, `strategyType` INTEGER NOT NULL, `rubrics` TEXT NOT NULL, `isFrozen` INTEGER NOT NULL, `text` TEXT NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `ReviewSession` (`id` INTEGER NOT NULL, `instruction` INTEGER NOT NULL, `submission` INTEGER NOT NULL, `givenReviews` TEXT NOT NULL, `isGivingStarted` INTEGER NOT NULL, `isGivingFinished` INTEGER NOT NULL, `takenReviews` TEXT NOT NULL, `isTakingStarted` INTEGER NOT NULL, `isTakingFinished` INTEGER NOT NULL, `isTakingFinishedByTeacher` INTEGER NOT NULL, `whenTakingFinishedByTeacher` INTEGER, `isReviewAvailable` INTEGER NOT NULL, `isFinished` INTEGER NOT NULL, `score` REAL NOT NULL, `availableReviewsCount` INTEGER, `activeReview` INTEGER, `finish` INTEGER NOT NULL, PRIMARY KEY(`id`))")
    }
}