package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.course_collection.structure.DbStructureCourseCollection

object MigrationFrom63To64 : Migration(63, 64) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ${DbStructureCourseCollection.TABLE_NAME} ADD COLUMN ${DbStructureCourseCollection.Columns.SIMILAR_AUTHORS} TEXT")
        db.execSQL("ALTER TABLE ${DbStructureCourseCollection.TABLE_NAME} ADD COLUMN ${DbStructureCourseCollection.Columns.SIMILAR_COURSE_LISTS} TEXT")
    }
}