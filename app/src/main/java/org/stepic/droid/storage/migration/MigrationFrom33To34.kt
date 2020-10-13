package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.persistence.model.PersistentItem
import org.stepic.droid.persistence.model.PersistentState
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentItem
import org.stepic.droid.persistence.storage.structure.DBStructurePersistentState
import org.stepic.droid.storage.structure.DbStructureBlock
import org.stepic.droid.storage.structure.DbStructureCachedVideo
import org.stepic.droid.storage.structure.DbStructureLesson
import org.stepic.droid.storage.structure.DbStructureSections
import org.stepic.droid.storage.structure.DbStructureStep
import org.stepic.droid.storage.structure.DbStructureUnit
import org.stepic.droid.storage.structure.DbStructureVideoUrl

object MigrationFrom33To34 : Migration(33, 34) {
    override fun migrate(db: SupportSQLiteDatabase) {
        DBStructurePersistentItem.createTable(db)
        DBStructurePersistentState.createTable(db)

        migrateToPersistentState(db)
        migrateCachedVideo(db)
    }

    private fun migrateToPersistentState(db: SupportSQLiteDatabase) {
        migrateStepsToPersistentState(db)
        migrateLessonsToPersistentState(db)
        migrateUnitsToPersistentState(db)
        migrateSectionsToPersistentState(db)
    }

    private fun migrateStepsToPersistentState(db: SupportSQLiteDatabase) {
        val sql =
                "REPLACE INTO ${DBStructurePersistentState.TABLE_NAME} " +
                "SELECT ${DbStructureStep.Column.STEP_ID}, ?, ? FROM ${DbStructureStep.STEPS} WHERE ${DbStructureStep.Column.IS_CACHED} = 1"

        db.execSQL(sql, arrayOf(PersistentState.Type.STEP.name, PersistentState.State.CACHED))
    }

    private fun migrateLessonsToPersistentState(db: SupportSQLiteDatabase) {
        val sql =
                "REPLACE INTO ${DBStructurePersistentState.TABLE_NAME} " +
                "SELECT ${DbStructureLesson.Column.LESSON_ID}, ?, ? FROM ${DbStructureLesson.LESSONS} WHERE ${DbStructureLesson.Column.IS_CACHED} = 1"

        db.execSQL(sql, arrayOf(PersistentState.Type.LESSON.name, PersistentState.State.CACHED))
    }

    private fun migrateUnitsToPersistentState(db: SupportSQLiteDatabase) {
        val sql =
                "REPLACE INTO ${DBStructurePersistentState.TABLE_NAME} " +
                "SELECT ${DbStructureUnit.UNITS}.${DbStructureUnit.Column.UNIT_ID}, ?, ? FROM ${DbStructureUnit.UNITS} " +
                "JOIN ${DbStructureLesson.LESSONS} " +
                        "ON ${DbStructureUnit.UNITS}.${DbStructureUnit.Column.LESSON} = ${DbStructureLesson.LESSONS}.${DbStructureLesson.Column.LESSON_ID} " +
                "WHERE ${DbStructureLesson.LESSONS}.${DbStructureLesson.Column.IS_CACHED} = 1"

        db.execSQL(sql, arrayOf(PersistentState.Type.UNIT.name, PersistentState.State.CACHED))
    }

    private fun migrateSectionsToPersistentState(db: SupportSQLiteDatabase) {
        val sql =
                "REPLACE INTO ${DBStructurePersistentState.TABLE_NAME} " +
                "SELECT ${DbStructureSections.Column.SECTION_ID}, ?, ? FROM ${DbStructureSections.SECTIONS} WHERE ${DbStructureSections.Column.IS_CACHED} = 1"

        db.execSQL(sql, arrayOf(PersistentState.Type.SECTION.name, PersistentState.State.CACHED))
    }

    private fun migrateCachedVideo(db: SupportSQLiteDatabase) {
        migrateThumbnails(db)
        migrateVideo(db)
    }

    private fun migrateVideo(db: SupportSQLiteDatabase) = migrateFromCachedVideos(
            db,
            "${DbStructureVideoUrl.externalVideosName}.${DbStructureVideoUrl.Column.url}",
            "${DbStructureCachedVideo.CACHED_VIDEO}.${DbStructureCachedVideo.Column.URL}",
            "JOIN ${DbStructureVideoUrl.externalVideosName} " +
                    "ON ${DbStructureCachedVideo.CACHED_VIDEO}.${DbStructureCachedVideo.Column.VIDEO_ID} = " +
                    "${DbStructureVideoUrl.externalVideosName}.${DbStructureVideoUrl.Column.videoId} " +
                    "AND ${DbStructureCachedVideo.CACHED_VIDEO}.${DbStructureCachedVideo.Column.QUALITY} = " +
                    "${DbStructureVideoUrl.externalVideosName}.${DbStructureVideoUrl.Column.quality} "
    )

    private fun migrateThumbnails(db: SupportSQLiteDatabase) = migrateFromCachedVideos(
            db,
            "${DbStructureBlock.BLOCKS}.${DbStructureBlock.Column.EXTERNAL_THUMBNAIL}",
            "${DbStructureCachedVideo.CACHED_VIDEO}.${DbStructureCachedVideo.Column.THUMBNAIL}",
            "JOIN ${DbStructureBlock.BLOCKS} " +
                    "ON ${DbStructureCachedVideo.CACHED_VIDEO}.${DbStructureCachedVideo.Column.VIDEO_ID} = " +
                    "${DbStructureBlock.BLOCKS}.${DbStructureBlock.Column.EXTERNAL_VIDEO_ID} "
    )

    private fun migrateFromCachedVideos(
            db: SupportSQLiteDatabase,
            originalPathSelector: String,
            localFileNameSelector: String,
            urlTableJoining: String
    ) {
        val sql =
                "REPLACE INTO ${DBStructurePersistentItem.PERSISTENT_ITEMS} " +
                        "SELECT " +
                        "$originalPathSelector as ${DBStructurePersistentItem.Columns.ORIGINAL_PATH}, " +
                        "$localFileNameSelector as ${DBStructurePersistentItem.Columns.LOCAL_FILE_NAME}, " +
                        "? as ${DBStructurePersistentItem.Columns.LOCAL_FILE_DIR}, " + // ''
                        "? as ${DBStructurePersistentItem.Columns.IS_IN_APP_INTERNAL_DIR}, " + // 0
                        "? as ${DBStructurePersistentItem.Columns.DOWNLOAD_ID}, " + // -1
                        "? as ${DBStructurePersistentItem.Columns.STATUS}, " +  // PersistentItem.Status.COMPLETED
                        "${DbStructureSections.SECTIONS}.${DbStructureSections.Column.COURSE} as ${DBStructurePersistentItem.Columns.COURSE}, " +
                        "${DbStructureSections.SECTIONS}.${DbStructureSections.Column.SECTION_ID} as ${DBStructurePersistentItem.Columns.SECTION}, " +
                        "${DbStructureUnit.UNITS}.${DbStructureUnit.Column.UNIT_ID} as ${DBStructurePersistentItem.Columns.UNIT}, " +
                        "${DbStructureStep.STEPS}.${DbStructureStep.Column.LESSON_ID} as ${DBStructurePersistentItem.Columns.LESSON}, " +
                        "${DbStructureCachedVideo.CACHED_VIDEO}.${DbStructureCachedVideo.Column.STEP_ID} as ${DBStructurePersistentItem.Columns.STEP} " +
                        "FROM " +
                        "${DbStructureCachedVideo.CACHED_VIDEO} " +
                        urlTableJoining +

                        "JOIN ${DbStructureStep.STEPS} " +
                        "ON ${DbStructureStep.STEPS}.${DbStructureStep.Column.STEP_ID} = " +
                        "${DbStructureCachedVideo.CACHED_VIDEO}.${DbStructureCachedVideo.Column.STEP_ID} " +

                        "JOIN ${DbStructureUnit.UNITS} " +
                        "ON ${DbStructureStep.STEPS}.${DbStructureStep.Column.LESSON_ID} = " +
                        "${DbStructureUnit.UNITS}.${DbStructureUnit.Column.LESSON} " +

                        "JOIN ${DbStructureSections.SECTIONS} " +
                        "ON ${DbStructureSections.SECTIONS}.${DbStructureSections.Column.SECTION_ID} = " +
                        "${DbStructureUnit.UNITS}.${DbStructureUnit.Column.SECTION}"

        db.execSQL(sql, arrayOf("", 0, -1, PersistentItem.Status.COMPLETED.name))
    }
}