package org.stepic.droid.storage.structure

object DbStructureCourse {
    const val TABLE_NAME = "course"

    object Columns {
        const val ID = "id"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val COVER = "cover"
        const val CERTIFICATE = "certificate"

        const val REQUIREMENTS = "requirements"
        const val SUMMARY = "summary"
        const val WORKLOAD = "workload"
        const val INTRO = "intro"
        const val INTRO_VIDEO_ID = "intro_video_id"
        const val LANGUAGE = "language"

        const val AUTHORS = "authors"
        const val INSTRUCTORS = "instructors"
        const val SECTIONS = "sections"

        const val COURSE_FORMAT = "course_format"
        const val TARGET_AUDIENCE = "target_audience"
        const val CERTIFICATE_FOOTER = "certificate_footer"
        const val CERTIFICATE_COVER_ORG = "certificate_cover_org"

        const val TOTAL_UNITS = "total_units"

        const val ENROLLMENT = "enrollment"
        const val PROGRESS = "progress"
        const val OWNER = "owner"

        const val IS_CONTEST = "is_contest"
        const val IS_FEATURED = "is_featured"
        const val IS_ACTIVE = "is_active"
        const val IS_PUBLIC = "is_public"

        const val CERTIFICATE_DISTINCTION_THRESHOLD = "certificate_distinction_threshold"
        const val CERTIFICATE_REGULAR_THRESHOLD = "certificate_regular_threshold"
        const val CERTIFICATE_LINK = "certificate_link"
        const val IS_CERTIFICATE_AUTO_ISSUED = "is_certificate_auto_issued"

        const val LAST_DEADLINE = "last_deadline"
        const val BEGIN_DATE = "begin_date"
        const val END_DATE = "end_date"

        const val SLUG = "slug"

        const val SCHEDULE_LINK = "schedule_link"
        const val SCHEDULE_LONG_LINK = "schedule_long_link"

        const val LAST_STEP = "last_step"
        const val LEARNERS_COUNT = "learners_count"
        const val REVIEW_SUMMARY = "review_summary"

        const val TIME_TO_COMPLETE = "time_to_complete"
    }
}