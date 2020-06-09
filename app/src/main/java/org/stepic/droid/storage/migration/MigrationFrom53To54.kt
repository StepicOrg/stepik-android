package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureCourseList
import org.stepik.android.cache.course_collection.structure.DbStructureCourseCollection
import org.stepik.android.cache.user_courses.structure.DbStructureUserCourse

object MigrationFrom53To54 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        db.execSQL(DbStructureCourseCollection.TABLE_SCHEMA)
    }
}