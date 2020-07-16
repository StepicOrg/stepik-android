package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.course_list.structure.DbStructureCourseListQuery

object MigrationFrom55To56 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        db.execSQL(DbStructureCourseListQuery.TABLE_SCHEMA)
    }
}