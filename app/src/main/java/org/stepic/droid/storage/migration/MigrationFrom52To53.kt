package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.user_courses.structure.DbStructureUserCourse

object MigrationFrom52To53 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        db.execSQL(DbStructureUserCourse.TABLE_SCHEMA)
    }
}