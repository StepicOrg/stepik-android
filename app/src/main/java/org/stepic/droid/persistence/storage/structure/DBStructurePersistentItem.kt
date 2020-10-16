package org.stepic.droid.persistence.storage.structure

import androidx.sqlite.db.SupportSQLiteDatabase

object DBStructurePersistentItem {
    const val PERSISTENT_ITEMS = "persistent_items"

    object Columns {
        const val ORIGINAL_PATH = "original_path"
        const val LOCAL_FILE_NAME = "local_file_name"
        const val LOCAL_FILE_DIR = "local_file_dir"
        const val IS_IN_APP_INTERNAL_DIR = "is_in_app_internal_dir"

        const val DOWNLOAD_ID = "download_id"
        const val STATUS = "status"

        const val COURSE = "course"
        const val SECTION = "section"
        const val LESSON = "lesson"
        const val UNIT = "unit"
        const val STEP = "step"
    }

    fun createTable(db: SupportSQLiteDatabase) {
        val sql = """
            CREATE TABLE IF NOT EXISTS $PERSISTENT_ITEMS (
                ${Columns.ORIGINAL_PATH} TEXT,
                ${Columns.LOCAL_FILE_NAME} TEXT,
                ${Columns.LOCAL_FILE_DIR} TEXT,
                ${Columns.IS_IN_APP_INTERNAL_DIR} INTEGER,

                ${Columns.DOWNLOAD_ID} LONG,
                ${Columns.STATUS} TEXT,

                ${Columns.COURSE} LONG,
                ${Columns.SECTION} LONG,
                ${Columns.LESSON} LONG,
                ${Columns.UNIT} LONG,
                ${Columns.STEP} LONG,

                PRIMARY KEY(${Columns.STEP}, ${Columns.ORIGINAL_PATH})
            )""".trimIndent()
        db.execSQL(sql)
    }
}