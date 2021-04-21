package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureCourse
import org.stepik.android.cache.section.structure.DbStructureSection

object MigrationFrom67To68 : Migration(67, 68) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureCourse.TABLE_NAME} ADD COLUMN ${DbStructureCourse.Columns.IS_PROCTORED} INTEGER")
        db.execSQL("ALTER TABLE ${DbStructureSection.TABLE_NAME} ADD COLUMN ${DbStructureSection.Columns.EXAM_DURATION_MINUTES} INTEGER")
        db.execSQL("ALTER TABLE ${DbStructureSection.TABLE_NAME} ADD COLUMN ${DbStructureSection.Columns.EXAM_SESSION} LONG")
        db.execSQL("ALTER TABLE ${DbStructureSection.TABLE_NAME} ADD COLUMN ${DbStructureSection.Columns.PROCTOR_SESSION} LONG")
        db.execSQL("ALTER TABLE ${DbStructureSection.TABLE_NAME} ADD COLUMN ${DbStructureSection.Columns.IS_PROCTORING_CAN_BE_SCHEDULED} INTEGER")
        db.execSQL("CREATE TABLE IF NOT EXISTS `ExamSession` (`id` INTEGER NOT NULL, `user` INTEGER NOT NULL, `section` INTEGER NOT NULL, `beginDate` INTEGER, `endDate` INTEGER, `timeLeft` REAL NOT NULL, `randomExam` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `ProctorSession` (`id` INTEGER NOT NULL, `user` INTEGER NOT NULL, `section` INTEGER NOT NULL, `createDate` INTEGER, `startUrl` TEXT NOT NULL, `stopUrl` TEXT NOT NULL, `startDate` INTEGER, `stopDate` INTEGER, `submitDate` INTEGER, `comment` TEXT NOT NULL, `score` REAL NOT NULL, PRIMARY KEY(`id`))")
    }
}