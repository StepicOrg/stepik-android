package org.stepic.droid.features.deadlines.storage

object DbStructureDeadlines {
    const val DEADLINES = "personal_deadlines"

    object Columns {
        const val RECORD_ID = "record_id"
        const val COURSE_ID = "course_id"
        const val SECTION_ID = "section_id"
        const val DEADLINE = "deadline"
    }
}