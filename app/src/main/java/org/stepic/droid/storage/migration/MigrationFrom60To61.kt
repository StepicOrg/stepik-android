package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object MigrationFrom60To61 : Migration(60, 61) {
    private const val TRIGGER_NAME = "visited_courses_limiter"
    private const val TABLE_SIZE_LIMIT = 20
    override fun migrate(db: SupportSQLiteDatabase) {
        val trigger = """
            CREATE TRIGGER IF NOT EXISTS $TRIGGER_NAME
            AFTER INSERT ON VisitedCourse
            BEGIN
            DELETE FROM VisitedCourse
            WHERE id
            IN
            (SELECT * FROM VisitedCourse ORDER BY id DESC
            LIMIT -1 OFFSET $TABLE_SIZE_LIMIT);
            END;
        """.trimIndent()
        db.execSQL("CREATE TABLE IF NOT EXISTS `VisitedCourse` (`id` INTEGER NOT NULL, `course` INTEGER NOT NULL, PRIMARY KEY(`course`))")
        db.execSQL(trigger)
    }
}