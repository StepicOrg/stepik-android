package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureCourse
import org.stepik.android.cache.certificates.structure.DbStructureCertificate

object MigrationFrom78To79 : Migration(78, 79) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureCertificate.TABLE_NAME} ADD COLUMN ${DbStructureCertificate.Columns.IS_WITH_SCORE} INTEGER")
        db.execSQL("ALTER TABLE ${DbStructureCourse.TABLE_NAME} ADD COLUMN ${DbStructureCourse.Columns.ANNOUNCEMENTS} TEXT")
        db.execSQL("CREATE TABLE IF NOT EXISTS `Announcement` (`id` INTEGER NOT NULL, `course` INTEGER NOT NULL, `user` INTEGER, `subject` TEXT NOT NULL, `text` TEXT NOT NULL, `createDate` INTEGER, `nextDate` INTEGER, `sentDate` INTEGER, `status` TEXT NOT NULL, `isRestrictedByScore` INTEGER NOT NULL, `scorePercentMin` INTEGER NOT NULL, `scorePercentMax` INTEGER NOT NULL, `emailTemplate` TEXT, `isScheduled` INTEGER NOT NULL, `startDate` INTEGER, `mailPeriodDays` INTEGER NOT NULL, `mailQuantity` INTEGER NOT NULL, `isInfinite` INTEGER NOT NULL, `onEnroll` INTEGER NOT NULL, `publishCount` INTEGER, `queueCount` INTEGER, `sentCount` INTEGER, `openCount` INTEGER, `clickCount` INTEGER, `estimatedStartDate` INTEGER, `estimatedFinishDate` INTEGER, `noticeDates` TEXT NOT NULL, PRIMARY KEY(`id`))")
    }
}