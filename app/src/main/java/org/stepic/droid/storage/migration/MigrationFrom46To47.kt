package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepik.android.cache.discussion_thread.structure.DbStructureDiscussionThread
import org.stepik.android.cache.lesson.structure.DbStructureLesson
import org.stepik.android.cache.step.structure.DbStructureStep

object MigrationFrom46To47 : Migration(46, 47) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            ALTER TABLE ${DbStructureLesson.TABLE_NAME} ADD COLUMN ${DbStructureLesson.Columns.ACTIONS} TEXT
        """.trimIndent())

        db.execSQL("""
            ALTER TABLE ${DbStructureStep.TABLE_NAME} ADD COLUMN ${DbStructureStep.Column.DISCUSSION_THREADS} TEXT
        """.trimIndent())

        DbStructureDiscussionThread.createTable(db)
    }
}