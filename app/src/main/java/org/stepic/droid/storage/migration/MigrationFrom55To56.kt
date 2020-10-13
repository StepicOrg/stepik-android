package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.course_list.structure.DbStructureCourseListQuery

object MigrationFrom55To56 : Migration(55, 56) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(DbStructureCourseListQuery.TABLE_SCHEMA)
    }
}