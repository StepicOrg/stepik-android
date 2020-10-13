package org.stepic.droid.persistence.storage.structure

import androidx.sqlite.db.SupportSQLiteDatabase

object DBStructurePersistentState {
    const val TABLE_NAME = "persistent_state"

    object Columns {
        const val ID = "id"
        const val TYPE = "type"
        const val STATE = "state"
    }

    fun createTable(db: SupportSQLiteDatabase) {
        val sql = """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${Columns.ID} LONG,
                ${Columns.TYPE} TEXT,
                ${Columns.STATE} TEXT,

                PRIMARY KEY(${Columns.ID}, ${Columns.TYPE})
            )""".trimIndent()
        db.execSQL(sql)
    }
}