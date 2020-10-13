package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.course_collection.structure.DbStructureCourseCollection

object MigrationFrom53To54 : Migration(53, 54) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(DbStructureCourseCollection.TABLE_SCHEMA)
    }
}