package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepic.droid.storage.structure.*
import org.stepik.android.cache.lesson.structure.DbStructureLesson
import org.stepik.android.cache.unit.structure.DbStructureUnit
import org.stepik.android.cache.user.structure.DbStructureUser
import org.stepik.android.cache.video.structure.VideoDbScheme
import org.stepik.android.cache.video.structure.VideoUrlDbScheme

object MigrationFrom36To37 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        migrateUser(db)
        migrateLastStep(db)
        migrateCourses(db)
        migrateBlockVideos(db)
        migrateLessons(db)
        migrateUnits(db)
    }

    private fun migrateUser(db: SQLiteDatabase) {
        DbStructureUser.createTable(db)
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

    private fun migrateLessons(db: SQLiteDatabase) {
        DbStructureLesson.createTable(db)

        db.execSQL("""
            INSERT INTO ${DbStructureLesson.TABLE_NAME}
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

    private fun migrateUnits(db: SQLiteDatabase) {
        DbStructureUnit.createTable(db)

        db.execSQL("""
            INSERT INTO ${DbStructureUnit.TABLE_NAME}
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
}