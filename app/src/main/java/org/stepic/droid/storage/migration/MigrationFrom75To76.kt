package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureCourse

object MigrationFrom75To76 : Migration(75, 76) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureCourse.TABLE_NAME} ADD COLUMN ${DbStructureCourse.Columns.IS_IN_WISHLIST} INTEGER")
        db.execSQL("CREATE TABLE IF NOT EXISTS `WishlistEntry` (`id` INTEGER NOT NULL, `course` INTEGER NOT NULL, `user` INTEGER NOT NULL, `createDate` INTEGER NOT NULL, `platform` TEXT NOT NULL, PRIMARY KEY(`id`))")
    }
}