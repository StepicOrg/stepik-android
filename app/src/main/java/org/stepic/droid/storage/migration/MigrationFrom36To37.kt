package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureCourse
import org.stepic.droid.storage.structure.DbStructureCourseList
import org.stepic.droid.storage.structure.DbStructureLastStep
import org.stepik.android.cache.video.structure.VideoDbScheme
import org.stepik.android.cache.video.structure.VideoUrlDbScheme

object MigrationFrom36To37 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        migrateLastStep(db)
        migrateCourses(db)
    }

    private fun migrateLastStep(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS last_steps")
        DbStructureLastStep.createTable(db)
    }

    private fun migrateCourses(db: SQLiteDatabase) {
        DbStructureCourse.createTable(db)
        DbStructureCourseList.createTable(db)
        VideoDbScheme.createTable(db)
        VideoUrlDbScheme.createTable(db)
    }
}