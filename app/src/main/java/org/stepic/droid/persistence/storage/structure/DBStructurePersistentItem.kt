package org.stepic.droid.persistence.storage.structure

object DBStructurePersistentItem {
    const val PERSISTENT_ITEMS = "persistent_items"

    object Columns {
        const val ORIGINAL_PATH = "original_path"
        const val LOCAL_FILE_NAME = "local_file_name"
        const val LOCAL_FILE_DIR = "local_file_dir"
        const val IS_IN_APP_INTERNAL_DIR = "is_is_app_internal_dir"

        const val DOWNLOAD_ID = "download_id"
        const val STATUS = "status"

        const val COURSE = "course"
        const val SECTION = "section"
        const val LESSON = "lesson"
        const val UNIT = "unit"
        const val STEP = "step"
    }
}