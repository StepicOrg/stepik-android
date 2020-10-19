package org.stepik.android.cache.lesson.structure

import androidx.sqlite.db.SupportSQLiteDatabase

object DbStructureLesson {
    const val TABLE_NAME = "lesson"

    object Columns {
        const val ID = "id"
        const val TITLE = "title"
        const val SLUG = "slug"
        const val COVER_URL = "cover_url"
        const val COURSES = "courses"
        const val STEPS = "steps"
        const val ACTIONS = "actions"
        const val IS_FEATURED = "is_featured"
        const val PROGRESS = "progress"
        const val OWNER = "owner"
        const val SUBSCRIPTIONS = "subscriptions"
        const val VIEWED_BY = "viewed_by"
        const val PASSED_BY = "passed_by"
        const val VOTE_DELTA = "vote_delta"
        const val LANGUAGE = "language"
        const val IS_PUBLIC = "is_public"
        const val CREATE_DATE = "create_date"
        const val UPDATE_DATE = "update_date"
        const val LEARNERS_GROUP = "learners_group"
        const val TEACHERS_GROUP = "teachers_group"
        const val TIME_TO_COMPLETE = "time_to_complete"
    }

    fun createTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${DbStructureLesson.Columns.ID} LONG PRIMARY KEY,
                ${DbStructureLesson.Columns.TITLE} TEXT,
                ${DbStructureLesson.Columns.SLUG} TEXT,
                ${DbStructureLesson.Columns.COVER_URL} TEXT,
                ${DbStructureLesson.Columns.STEPS} TEXT,
                ${DbStructureLesson.Columns.IS_FEATURED} INTEGER,
                ${DbStructureLesson.Columns.PROGRESS} TEXT,
                ${DbStructureLesson.Columns.OWNER} LONG,
                ${DbStructureLesson.Columns.SUBSCRIPTIONS} TEXT,
                ${DbStructureLesson.Columns.VIEWED_BY} LONG,
                ${DbStructureLesson.Columns.PASSED_BY} LONG,
                ${DbStructureLesson.Columns.VOTE_DELTA} LONG,
                ${DbStructureLesson.Columns.LANGUAGE} TEXT,
                ${DbStructureLesson.Columns.IS_PUBLIC} INTEGER,
                ${DbStructureLesson.Columns.CREATE_DATE} LONG,
                ${DbStructureLesson.Columns.UPDATE_DATE} LONG,
                ${DbStructureLesson.Columns.LEARNERS_GROUP} TEXT,
                ${DbStructureLesson.Columns.TEACHERS_GROUP} TEXT,
                ${DbStructureLesson.Columns.TIME_TO_COMPLETE} LONG
            )
        """.trimIndent())
    }
}