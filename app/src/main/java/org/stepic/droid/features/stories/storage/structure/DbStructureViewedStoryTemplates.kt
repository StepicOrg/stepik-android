package org.stepic.droid.features.stories.storage.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureViewedStoryTemplates {
    const val VIEWED_STORY_TEMPLATES = "viewed_story_templates"

    object Columns {
        const val ID = "id"
    }

    fun createTable(db: SQLiteDatabase) {
        val sql = """
            CREATE TABLE IF NOT EXISTS $VIEWED_STORY_TEMPLATES (
                ${Columns.ID} LONG,
                PRIMARY KEY(${Columns.ID})
            )""".trimIndent()

        db.execSQL(sql)
    }
}