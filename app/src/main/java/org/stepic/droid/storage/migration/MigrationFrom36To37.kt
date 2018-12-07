package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.storage.structure.*
import org.stepik.android.cache.video.structure.VideoDbScheme
import org.stepik.android.cache.video.structure.VideoUrlDbScheme

object MigrationFrom36To37 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        migrateLastStep(db)
        migrateCourses(db)
        migrateBlockVideos(db)
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

    private fun migrateBlockVideos(db: SQLiteDatabase) {
        // Migrate urls
        db.execSQL("""
            INSERT INTO ${VideoUrlDbScheme.TABLE_NAME}
            SELECT
                ${DbStructureVideoUrl.Column.videoId},
                ${DbStructureVideoUrl.Column.url},
                ${DbStructureVideoUrl.Column.quality}
            FROM ${DbStructureVideoUrl.externalVideosName}
        """.trimIndent())

        // Migrate videos
        db.execSQL("""
            INSERT INTO ${VideoDbScheme.TABLE_NAME}
            SELECT
                ${DbStructureBlock.Column.EXTERNAL_VIDEO_ID},
                ${DbStructureBlock.Column.EXTERNAL_THUMBNAIL},
                ${DbStructureBlock.Column.EXTERNAL_VIDEO_DURATION}
            FROM ${DbStructureBlock.BLOCKS}
            WHERE ${DbStructureBlock.BLOCKS}.${DbStructureBlock.Column.EXTERNAL_VIDEO_ID} > 0
        """.trimIndent())
    }
}