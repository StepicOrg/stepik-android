package org.stepik.android.cache.course_list.structure

object DbStructureCourseListQuery {
    const val TABLE_NAME = "course_list_query"

    object Columns {
        const val ID = "id"
        const val COURSES = "courses"
    }

    const val TABLE_SCHEMA =
        "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "${Columns.ID} TEXT PRIMARY KEY," +
            "${Columns.COURSES} TEXT" +
        ")"
}