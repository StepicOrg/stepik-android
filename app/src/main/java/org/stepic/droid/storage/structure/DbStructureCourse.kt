package org.stepic.droid.storage.structure

import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

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
        const val READINESS = "readiness"

        const val IS_CONTEST = "is_contest"
        const val IS_FEATURED = "is_featured"
        const val IS_ACTIVE = "is_active"
        const val IS_PUBLIC = "is_public"
        const val IS_ARCHIVED = "is_archived"
        const val IS_FAVORITE = "is_favorite"

        const val CERTIFICATE_DISTINCTION_THRESHOLD = "certificate_distinction_threshold"
        const val CERTIFICATE_REGULAR_THRESHOLD = "certificate_regular_threshold"
        const val CERTIFICATE_LINK = "certificate_link"
        const val IS_CERTIFICATE_AUTO_ISSUED = "is_certificate_auto_issued"
        const val IS_CERTIFICATE_ISSUED = "is_certificate_issued"

        const val LAST_DEADLINE = "last_deadline"
        const val BEGIN_DATE = "begin_date"
        const val END_DATE = "end_date"

        const val SLUG = "slug"

        const val SCHEDULE_LINK = "schedule_link"
        const val SCHEDULE_LONG_LINK = "schedule_long_link"
        const val SCHEDULE_TYPE = "schedule_type"

        const val LAST_STEP = "last_step"
        const val LEARNERS_COUNT = "learners_count"
        const val REVIEW_SUMMARY = "review_summary"

        const val TIME_TO_COMPLETE = "time_to_complete"
        const val OPTIONS = "options"

        const val IS_PAID = "is_paid"
        const val PRICE = "price"
        const val CURRENCY_CODE = "currency_code"
        const val DISPLAY_PRICE = "display_price"
        const val PRICE_TIER = "price_tier"
    }

    fun createTable(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS ${DbStructureCourse.TABLE_NAME} (
                ${DbStructureCourse.Columns.ID} LONG PRIMARY KEY,
                ${DbStructureCourse.Columns.TITLE} TEXT,
                ${DbStructureCourse.Columns.DESCRIPTION} TEXT,
                ${DbStructureCourse.Columns.COVER} TEXT,
                ${DbStructureCourse.Columns.CERTIFICATE} TEXT,
                ${DbStructureCourse.Columns.REQUIREMENTS} TEXT,
                ${DbStructureCourse.Columns.SUMMARY} TEXT,
                ${DbStructureCourse.Columns.WORKLOAD} TEXT,
                ${DbStructureCourse.Columns.INTRO} TEXT,
                ${DbStructureCourse.Columns.INTRO_VIDEO_ID} LONG,
                ${DbStructureCourse.Columns.LANGUAGE} TEXT,
                ${DbStructureCourse.Columns.AUTHORS} TEXT,
                ${DbStructureCourse.Columns.INSTRUCTORS} TEXT,
                ${DbStructureCourse.Columns.SECTIONS} TEXT,
                ${DbStructureCourse.Columns.COURSE_FORMAT} TEXT,
                ${DbStructureCourse.Columns.TARGET_AUDIENCE} TEXT,
                ${DbStructureCourse.Columns.CERTIFICATE_FOOTER} TEXT,
                ${DbStructureCourse.Columns.CERTIFICATE_COVER_ORG} TEXT,
                ${DbStructureCourse.Columns.TOTAL_UNITS} LONG,
                ${DbStructureCourse.Columns.ENROLLMENT} LONG,
                ${DbStructureCourse.Columns.PROGRESS} TEXT,
                ${DbStructureCourse.Columns.OWNER} LONG,
                ${DbStructureCourse.Columns.READINESS} REAL,
                ${DbStructureCourse.Columns.IS_CONTEST} INTEGER,
                ${DbStructureCourse.Columns.IS_FEATURED} INTEGER,
                ${DbStructureCourse.Columns.IS_ACTIVE} INTEGER,
                ${DbStructureCourse.Columns.IS_PUBLIC} INTEGER,
                ${DbStructureCourse.Columns.CERTIFICATE_DISTINCTION_THRESHOLD} INTEGER,
                ${DbStructureCourse.Columns.CERTIFICATE_REGULAR_THRESHOLD} INTEGER,
                ${DbStructureCourse.Columns.CERTIFICATE_LINK} TEXT,
                ${DbStructureCourse.Columns.IS_CERTIFICATE_AUTO_ISSUED} INTEGER,
                ${DbStructureCourse.Columns.LAST_DEADLINE} TEXT,
                ${DbStructureCourse.Columns.BEGIN_DATE} TEXT,
                ${DbStructureCourse.Columns.END_DATE} TEXT,
                ${DbStructureCourse.Columns.SLUG} TEXT,
                ${DbStructureCourse.Columns.SCHEDULE_LINK} TEXT,
                ${DbStructureCourse.Columns.SCHEDULE_LONG_LINK} TEXT,
                ${DbStructureCourse.Columns.SCHEDULE_TYPE} TEXT,
                ${DbStructureCourse.Columns.LAST_STEP} TEXT,
                ${DbStructureCourse.Columns.LEARNERS_COUNT} LONG,
                ${DbStructureCourse.Columns.REVIEW_SUMMARY} LONG,
                ${DbStructureCourse.Columns.TIME_TO_COMPLETE} LONG,

                ${DbStructureCourse.Columns.IS_PAID} INTEGER,
                ${DbStructureCourse.Columns.PRICE} TEXT,
                ${DbStructureCourse.Columns.CURRENCY_CODE} TEXT,
                ${DbStructureCourse.Columns.DISPLAY_PRICE} TEXT,
                ${DbStructureCourse.Columns.PRICE_TIER} TEXT
            )
        """.trimIndent())
    }
}