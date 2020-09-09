package org.stepic.droid.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.stepic.droid.storage.structure.DbStructureBlock
import org.stepic.droid.storage.structure.DbStructureCourse
import org.stepic.droid.storage.structure.DbStructureCourseList
import org.stepic.droid.storage.structure.DbStructureLastStep
import org.stepic.droid.storage.structure.DbStructureProgress
import org.stepic.droid.storage.structure.DbStructureSections
import org.stepic.droid.storage.structure.DbStructureVideoUrl
import org.stepik.android.cache.lesson.structure.DbStructureLesson
import org.stepik.android.cache.section.structure.DbStructureSection
import org.stepik.android.cache.unit.structure.DbStructureUnit
import org.stepik.android.cache.user.structure.DbStructureUser
import org.stepik.android.cache.video.structure.VideoDbScheme
import org.stepik.android.cache.video.structure.VideoUrlDbScheme

object MigrationFrom36To37 : Migration(36, 37) {
    override fun migrate(db: SupportSQLiteDatabase) {
        migrateUser(db)
        migrateLastStep(db)
        migrateCourses(db)
        migrateBlockVideos(db)
        migrateLessons(db)
        migrateUnits(db)
        migrateSections(db)
        migrateProgress(db)
    }

    private fun migrateUser(db: SupportSQLiteDatabase) {
        DbStructureUser.createTable(db)
    }

    private fun migrateLastStep(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS last_steps")
        DbStructureLastStep.createTable(db)
    }

    private fun migrateCourses(db: SupportSQLiteDatabase) {
        DbStructureCourse.createTable(db)
        DbStructureCourseList.createTable(db)
    }

    private fun migrateBlockVideos(db: SupportSQLiteDatabase) {
        VideoDbScheme.createTable(db)
        VideoUrlDbScheme.createTable(db)

        // Migrate urls
        db.execSQL("""
            REPLACE INTO ${VideoUrlDbScheme.TABLE_NAME}
            SELECT
                ${DbStructureVideoUrl.Column.videoId},
                ${DbStructureVideoUrl.Column.url},
                ${DbStructureVideoUrl.Column.quality}
            FROM ${DbStructureVideoUrl.externalVideosName}
        """.trimIndent())

        // Migrate videos
        db.execSQL("""
            REPLACE INTO ${VideoDbScheme.TABLE_NAME}
            SELECT
                ${DbStructureBlock.Column.EXTERNAL_VIDEO_ID},
                ${DbStructureBlock.Column.EXTERNAL_THUMBNAIL},
                ${DbStructureBlock.Column.EXTERNAL_VIDEO_DURATION}
            FROM ${DbStructureBlock.BLOCKS}
            WHERE ${DbStructureBlock.BLOCKS}.${DbStructureBlock.Column.EXTERNAL_VIDEO_ID} > 0
        """.trimIndent())
    }

    private fun migrateLessons(db: SupportSQLiteDatabase) {
        DbStructureLesson.createTable(db)

        db.execSQL("""
            REPLACE INTO ${DbStructureLesson.TABLE_NAME}
            SELECT
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.LESSON_ID},
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.TITLE},
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.SLUG},
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.COVER_URL},
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.STEPS},
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.IS_FEATURED},
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.PROGRESS},
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.OWNER},
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.SUBSCRIPTIONS},
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.VIEWED_BY},
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.PASSED_BY},
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.VOTE_DELTA},
                NULL,
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.IS_PUBLIC},
                -1,
                -1,
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.LEARNERS_GROUP},
                ${org.stepic.droid.storage.structure.DbStructureLesson.Column.TEACHER_GROUP},
                0
            FROM ${org.stepic.droid.storage.structure.DbStructureLesson.LESSONS}
        """.trimIndent())
    }

    private fun migrateUnits(db: SupportSQLiteDatabase) {
        DbStructureUnit.createTable(db)

        db.execSQL("""
            REPLACE INTO ${DbStructureUnit.TABLE_NAME}
            SELECT
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.UNIT_ID},
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.SECTION},
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.LESSON},
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.ASSIGNMENTS},
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.POSITION},
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.PROGRESS},
                -1,
                -1,
                -1,
                -1,
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.GRADING_POLICY},
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.BEGIN_DATE_SOURCE},
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.END_DATE_SOURCE},
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.SOFT_DEADLINE_SOURCE},
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.HARD_DEADLINE_SOURCE},
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.GRADING_POLICY_SOURCE},
                ${org.stepic.droid.storage.structure.DbStructureUnit.Column.IS_ACTIVE},
                -1,
                -1
            FROM ${org.stepic.droid.storage.structure.DbStructureUnit.UNITS}
        """.trimIndent())
    }

    private fun migrateSections(db: SupportSQLiteDatabase) {
        DbStructureSection.createTable(db)

        db.execSQL("""
            REPLACE INTO ${DbStructureSection.TABLE_NAME}
            SELECT
                ${DbStructureSections.Column.SECTION_ID},
                ${DbStructureSections.Column.COURSE},
                ${DbStructureSections.Column.UNITS},
                ${DbStructureSections.Column.POSITION},
                ${DbStructureSections.Column.PROGRESS},
                ${DbStructureSections.Column.TITLE},
                ${DbStructureSections.Column.SLUG},
                -1,
                -1,
                -1,
                -1,
                -1,
                -1,
                ${DbStructureSections.Column.GRADING_POLICY},
                ${DbStructureSections.Column.IS_ACTIVE},
                ${DbStructureSections.Column.TEST_SECTION},
                ${DbStructureSections.Column.IS_EXAM},
                ${DbStructureSections.Column.DISCOUNTING_POLICY},
                ${DbStructureSections.Column.IS_REQUIREMENT_SATISFIED},
                ${DbStructureSections.Column.REQUIRED_SECTION},
                ${DbStructureSections.Column.REQUIRED_PERCENT}
            FROM ${DbStructureSections.SECTIONS}
        """.trimIndent())
    }

    private fun migrateProgress(db: SupportSQLiteDatabase) {
        val tmpTable = "progress_migration_37"
        db.execSQL("ALTER TABLE ${DbStructureProgress.TABLE_NAME} RENAME TO $tmpTable")

        DbStructureProgress.createTable(db)

        db.execSQL("""
            REPLACE INTO ${DbStructureProgress.TABLE_NAME}
            SELECT
                ${DbStructureProgress.Columns.ID},
                ${DbStructureProgress.Columns.LAST_VIEWED},
                ${DbStructureProgress.Columns.SCORE},
                ${DbStructureProgress.Columns.COST},
                ${DbStructureProgress.Columns.N_STEPS},
                ${DbStructureProgress.Columns.N_STEPS_PASSED},
                ${DbStructureProgress.Columns.IS_PASSED}
            FROM $tmpTable
        """.trimIndent())

        db.execSQL("DROP TABLE $tmpTable")
    }
}