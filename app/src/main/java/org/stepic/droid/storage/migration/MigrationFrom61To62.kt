package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.course_collection.structure.DbStructureCourseCollection

object MigrationFrom61To62 : Migration(61, 62) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureCourseCollection.TABLE_NAME} ADD COLUMN ${DbStructureCourseCollection.Columns.PLATFORM} INTEGER")
    }
}