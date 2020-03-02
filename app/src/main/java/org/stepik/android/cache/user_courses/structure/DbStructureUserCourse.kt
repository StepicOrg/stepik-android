package org.stepik.android.cache.user_courses.structure

object DbStructureUserCourse {
    const val TABLE_NAME = "user_courses"

    object Columns {
        const val ID = "id"
        const val USER = "user"
        const val COURSE = "course"
        const val IS_FAVORITE = "is_favorite"
        const val IS_PINNED = "is_pinned"
        const val IS_ARCHIVED = "is_archived"
        const val LAST_VIEWED = "last_viewed"
    }

    const val TABLE_SCHEMA =
        "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
            "${Columns.ID} LONG," +
            "${Columns.USER} LONG," +
            "${Columns.COURSE} LONG," +
            "${Columns.IS_FAVORITE} INTEGER," +
            "${Columns.IS_PINNED} INTEGER," +
            "${Columns.IS_ARCHIVED} INTEGER," +
            "${Columns.LAST_VIEWED} LONG," +
            "PRIMARY KEY (${Columns.USER}, ${Columns.COURSE})" +
        ")"
}