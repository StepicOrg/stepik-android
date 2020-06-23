package org.stepik.android.cache.analytic.structure

object DbStructureAnalytic {
    const val TABLE_NAME = "analytic"

    object Columns {
        const val ID = "id"
        const val EVENT_NAME = "event_name"
        const val EVENT_JSON = "event_json"
        const val EVENT_TIMESTAMP = "event_timestamp"
    }

    const val TABLE_SCHEMA =
        "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "${Columns.ID} LONG PRIMARY KEY AUTOINCREMENT," +
            "${Columns.EVENT_NAME} TEXT," +
            "${Columns.EVENT_JSON} TEXT," +
            "${Columns.EVENT_TIMESTAMP} LONG" +
        ")"
}