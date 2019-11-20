package org.stepik.android.cache.attempt.structure

object DbStructureAttempt {
    const val TABLE_NAME = "attempt"

    object Columns {
        const val ID = "id"
        const val STEP = "step"
        const val USER = "user"

        const val DATASET = "dataset"
        const val DATASET_URL = "dataset_url"

        const val STATUS = "status"
        const val TIME = "time"

        const val TIME_LEFT = "time_left"
    }

    const val TABLE_SCHEMA =
        "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "${Columns.ID} LONG," +
            "${Columns.STEP} LONG," +
            "${Columns.USER} LONG," +
            "${Columns.DATASET} TEXT," +
            "${Columns.DATASET_URL} TEXT," +
            "${Columns.STATUS} TEXT," +
            "${Columns.TIME} LONG," +
            "${Columns.TIME_LEFT} TEXT," +
            "PRIMARY KEY (${Columns.STEP}, ${Columns.USER})" +
        ")"
}