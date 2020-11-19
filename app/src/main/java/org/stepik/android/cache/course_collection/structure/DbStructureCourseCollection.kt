package org.stepik.android.cache.course_collection.structure

object DbStructureCourseCollection {
    const val TABLE_NAME = "course_collection"

    object Columns {
        const val ID = "id"
        const val POSITION = "position"
        const val TITLE = "title"
        const val LANGUAGE = "language"
        const val COURSES = "courses"
        const val DESCRIPTION = "description"
        const val PLATFORM = "platform"
    }

    const val TABLE_SCHEMA =
        "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "${Columns.ID} LONG PRIMARY KEY," +
            "${Columns.POSITION} INTEGER," +
            "${Columns.TITLE} TEXT," +
            "${Columns.LANGUAGE} TEXT," +
            "${Columns.COURSES} TEXT," +
            "${Columns.DESCRIPTION} TEXT" +
        ")"
}