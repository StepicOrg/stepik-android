package org.stepik.android.cache.section.structure

import android.database.sqlite.SQLiteDatabase

object DbStructureSection {
    const val TABLE_NAME = "section"

    object Columns {
        const val ID = "id"
        const val COURSE = "course"
        const val UNITS = "units"
        const val POSITION = "position"
        const val PROGRESS = "progress"
        const val TITLE = "title"
        const val SLUG = "slug"
        const val BEGIN_DATE = "begin_date"
        const val END_DATE = "end_date"
        const val SOFT_DEADLINE = "soft_deadline"
        const val HARD_DEADLINE = "hard_deadline"
        const val CREATE_DATE = "create_date"
        const val UPDATE_DATE = "update_date"
        const val GRADING_POLICY = "grading_policy"
        const val IS_ACTIVE = "is_active"
        const val ACTIONS_TEST_SECTION = "actions_test_section"
        const val IS_EXAM = "is_exam"
        const val DISCOUNTING_POLICY = "discounting_policy"
        const val IS_REQUIREMENT_SATISFIED = "is_requirement_satisfied"
        const val REQUIRED_SECTION = "required_section"
        const val REQUIRED_PERCENT = "required_percent"
    }

    fun createTable(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                ${DbStructureSection.Columns.ID} LONG PRIMARY KEY,
                ${DbStructureSection.Columns.COURSE} LONG,
                ${DbStructureSection.Columns.UNITS} TEXT,
                ${DbStructureSection.Columns.POSITION} INTEGER,
                ${DbStructureSection.Columns.PROGRESS} TEXT,
                ${DbStructureSection.Columns.TITLE} TEXT,
                ${DbStructureSection.Columns.SLUG} TEXT,
                ${DbStructureSection.Columns.BEGIN_DATE} LONG,
                ${DbStructureSection.Columns.END_DATE} LONG,
                ${DbStructureSection.Columns.SOFT_DEADLINE} LONG,
                ${DbStructureSection.Columns.HARD_DEADLINE} LONG,
                ${DbStructureSection.Columns.CREATE_DATE} LONG,
                ${DbStructureSection.Columns.UPDATE_DATE} LONG,
                ${DbStructureSection.Columns.GRADING_POLICY} TEXT,
                ${DbStructureSection.Columns.IS_ACTIVE} INTEGER,
                ${DbStructureSection.Columns.ACTIONS_TEST_SECTION} TEXT,
                ${DbStructureSection.Columns.IS_EXAM} INTEGER,
                ${DbStructureSection.Columns.DISCOUNTING_POLICY} LONG,
                ${DbStructureSection.Columns.IS_REQUIREMENT_SATISFIED} INTEGER,
                ${DbStructureSection.Columns.REQUIRED_SECTION} LONG,
                ${DbStructureSection.Columns.REQUIRED_PERCENT} INTEGER
            )
        """.trimIndent())
    }
}