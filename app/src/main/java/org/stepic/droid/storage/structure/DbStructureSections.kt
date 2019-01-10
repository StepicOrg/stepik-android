package org.stepic.droid.storage.structure

@Deprecated("Use cache.DbStructureSection")
object DbStructureSections {
    const val SECTIONS = "sections"

    object Column {
        @Deprecated("it is not used, use section_id instead")
        const val ID = "_id"
        const val SECTION_ID = "section_id"
        const val COURSE = "course"
        const val UNITS = "units"
        const val POSITION = "position"
        const val PROGRESS = "section_progress"
        const val TITLE = "title"
        const val SLUG = "slug"
        const val BEGIN_DATE = "begin_date"
        const val END_DATE = "end_date"
        const val SOFT_DEADLINE = "soft_deadline"
        const val HARD_DEADLINE = "hard_deadline"
        const val GRADING_POLICY = "grading_policy"
        const val BEGIN_DATE_SOURCE = "begin_data_source"
        const val END_DATE_SOURCE = "end_data_source"
        const val SOFT_DEADLINE_SOURCE = "soft_deadline_source"
        const val HARD_DEADLINE_SOURCE = "hard_deadline_source"
        const val GRADING_POLICY_SOURCE = "grading_policy_source"
        const val IS_ACTIVE = "is_active"
        const val CREATE_DATE = "create_date"
        const val UPDATE_DATE = "update_date"
        const val IS_CACHED = "is_cached"
        const val IS_LOADING = "is_loading"
        const val TEST_SECTION = "can_test_section"
        const val DISCOUNTING_POLICY = "discounting_policy"
        const val IS_EXAM = "is_exam"
        const val IS_REQUIREMENT_SATISFIED = "is_requirement_satisfied"
        const val REQUIRED_SECTION = "required_section"
        const val REQUIRED_PERCENT = "required_percent"
    }
}
