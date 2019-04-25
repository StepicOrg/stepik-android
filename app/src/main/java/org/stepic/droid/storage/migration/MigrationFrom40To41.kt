package org.stepic.droid.storage.migration

import android.database.sqlite.SQLiteDatabase
import org.stepik.android.cache.step.structure.DbStructureStep

object MigrationFrom40To41 : Migration {
    override fun migrate(db: SQLiteDatabase) {
        createStepsTable(db)
        migrateSteps(db)
    }

    private fun createStepsTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ${DbStructureStep.TABLE_NAME} (
                ${DbStructureStep.Column.ID} LONG PRIMARY KEY,
                ${DbStructureStep.Column.LESSON_ID} LONG,
                ${DbStructureStep.Column.POSITION} INTEGER,

                ${DbStructureStep.Column.STATUS} TEXT,
                ${DbStructureStep.Column.PROGRESS} TEXT,
                ${DbStructureStep.Column.SUBSCRIPTION} TEXT,

                ${DbStructureStep.Column.VIEWED_BY} LONG,
                ${DbStructureStep.Column.PASSED_BY} LONG,
                ${DbStructureStep.Column.WORTH} LONG,

                ${DbStructureStep.Column.CREATE_DATE} LONG,
                ${DbStructureStep.Column.UPDATE_DATE} LONG,

                ${DbStructureStep.Column.DISCUSSION_COUNT} INTEGER,
                ${DbStructureStep.Column.DISCUSSION_PROXY} TEXT,

                ${DbStructureStep.Column.PEER_REVIEW} TEXT,
                ${DbStructureStep.Column.HAS_SUBMISSION_RESTRICTION} BOOLEAN,
                ${DbStructureStep.Column.MAX_SUBMISSION_COUNT} INTEGER
            )
        """.trimIndent())
    }

    private fun migrateSteps(db: SQLiteDatabase) {
        db.execSQL("""
            REPLACE INTO ${DbStructureStep.TABLE_NAME}
            SELECT
                ${org.stepic.droid.storage.structure.DbStructureStep.Column.STEP_ID},
                ${org.stepic.droid.storage.structure.DbStructureStep.Column.LESSON_ID},
                ${org.stepic.droid.storage.structure.DbStructureStep.Column.POSITION},

                ${org.stepic.droid.storage.structure.DbStructureStep.Column.STATUS},
                ${org.stepic.droid.storage.structure.DbStructureStep.Column.PROGRESS},
                ${org.stepic.droid.storage.structure.DbStructureStep.Column.SUBSCRIPTIONS},

                ${org.stepic.droid.storage.structure.DbStructureStep.Column.VIEWED_BY},
                ${org.stepic.droid.storage.structure.DbStructureStep.Column.PASSED_BY},
                0,
                -1,
                -1,
                ${org.stepic.droid.storage.structure.DbStructureStep.Column.DISCUSSION_COUNT},
                ${org.stepic.droid.storage.structure.DbStructureStep.Column.DISCUSSION_ID},
                ${org.stepic.droid.storage.structure.DbStructureStep.Column.PEER_REVIEW},
                ${org.stepic.droid.storage.structure.DbStructureStep.Column.HAS_SUBMISSION_RESTRICTION},
                ${org.stepic.droid.storage.structure.DbStructureStep.Column.MAX_SUBMISSION_COUNT}
            FROM ${org.stepic.droid.storage.structure.DbStructureStep.STEPS}
        """.trimIndent())
    }
}