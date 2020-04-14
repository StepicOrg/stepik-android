package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureCourseList

object MigrationFrom53To54 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        DbStructureCourseList.dropTable(db)
    }
}