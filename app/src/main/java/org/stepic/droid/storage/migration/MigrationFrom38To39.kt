package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.comments.structure.DbStructureCommentsBanner
import org.stepik.android.cache.course_calendar.structure.DbStructureSectionDateEvent

object MigrationFrom38To39 : Migration{
    override fun migrate(db: SQLiteDatabase) {
        DbStructureSectionDateEvent.createTable(db)
        DbStructureCommentsBanner.createTable(db)
    }

}