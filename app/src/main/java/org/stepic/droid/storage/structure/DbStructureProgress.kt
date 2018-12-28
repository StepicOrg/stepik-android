package org.stepic.droid.storage.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureProgress {
    const val TABLE_NAME = "progress"
    val usedColumns: Array<String> =
        arrayOf(
            Columns.ID,
            Columns.LAST_VIEWED,
            Columns.SCORE,
            Columns.COST,
            Columns.N_STEPS,
            Columns.N_STEPS_PASSED,
            Columns.IS_PASSED
        )

    object Columns {
        const val ID = "progress_id"
        const val LAST_VIEWED = "last_viewed"
        const val SCORE = "score"
        const val COST = "cost"
        const val N_STEPS = "n_steps"
        const val N_STEPS_PASSED = "n_steps_passed"
        const val IS_PASSED = "is_passed"
    }

    fun createTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${DbStructureProgress.Columns.ID} TEXT PRIMARY KEY,
                ${DbStructureProgress.Columns.LAST_VIEWED} TEXT,
                ${DbStructureProgress.Columns.SCORE} TEXT,
                ${DbStructureProgress.Columns.COST} LONG,
                ${DbStructureProgress.Columns.N_STEPS} LONG,
                ${DbStructureProgress.Columns.N_STEPS_PASSED} LONG,
                ${DbStructureProgress.Columns.IS_PASSED} INTEGER
            )
        """.trimIndent())
    }
}
