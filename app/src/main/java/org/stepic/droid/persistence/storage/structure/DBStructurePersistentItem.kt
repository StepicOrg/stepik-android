package org.stepic.droid.persistence.storage.structure

object DBStructurePersistentItem {
    const val PERSISTENT_ITEMS = "persistent_items"

    object Columns {
        const val ORIGINAL_PATH = "original_path"
        const val LOCAL_PATH = "local_path"
        const val DOWNLOAD_ID = "download_id"
        const val STATUS = "status"

        const val COURSE = "course"
        const val SECTION = "section"
        const val LESSON = "lesson"
        const val UNIT = "unit"
        const val STEP = "step"
    }
}