package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureCourseList
import org.stepik.android.cache.user_courses.structure.DbStructureUserCourse

object MigrationFrom52To53 : Migration(52, 53) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(DbStructureUserCourse.TABLE_SCHEMA)
        DbStructureCourseList.dropTable(db)
    }
}